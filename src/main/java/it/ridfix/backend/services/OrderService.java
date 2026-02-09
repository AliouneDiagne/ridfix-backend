package it.ridfix.backend.services;

import it.ridfix.backend.dto.CommonDTOs;
import it.ridfix.backend.dto.OrderDTOs;
import it.ridfix.backend.entities.Address;
import it.ridfix.backend.entities.Order;
import it.ridfix.backend.entities.OrderItem;
import it.ridfix.backend.entities.Payment;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.enums.OrderStatus;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.AddressRepository;
import it.ridfix.backend.repositories.OrderItemRepository;
import it.ridfix.backend.repositories.OrderRepository;
import it.ridfix.backend.repositories.PaymentRepository;
import it.ridfix.backend.repositories.ProductRepository;
import it.ridfix.backend.repositories.UserRepository;
import it.ridfix.backend.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final ProductRepository products; // used in topSelling()
    private final AddressRepository addresses;
    private final UserRepository users;
    private final PaymentRepository payments;
    private final NotificationService notifications;
    private final InventoryService inventory;

    public OrderService(OrderRepository orders,
                        OrderItemRepository orderItems,
                        ProductRepository products,
                        AddressRepository addresses,
                        UserRepository users,
                        PaymentRepository payments,
                        NotificationService notifications,
                        InventoryService inventory) {
        this.orders = orders;
        this.orderItems = orderItems;
        this.products = products;
        this.addresses = addresses;
        this.users = users;
        this.payments = payments;
        this.notifications = notifications;
        this.inventory = inventory;
    }

    @Transactional
    public OrderDTOs.OrderResponse createOrder(OrderDTOs.CreateOrderRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));

        Address ship = addresses.findByIdAndUserId(req.shippingAddressId(), userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Shipping address not found"));
        Address bill = addresses.findByIdAndUserId(req.billingAddressId(), userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Billing address not found"));

        Order order = new Order(user, formatAddress(ship), formatAddress(bill));
        orders.save(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> createdItems = new ArrayList<>();

        // ✅ deterministic lock order
        List<OrderDTOs.OrderItemRequest> sortedItems = new ArrayList<>(req.items());
        sortedItems.sort(Comparator.comparing(OrderDTOs.OrderItemRequest::productId));

        for (OrderDTOs.OrderItemRequest item : sortedItems) {
            int qty = item.quantity();
            Product product = inventory.decrementStockForOrder(item.productId(), qty, order);

            OrderItem oi = new OrderItem(order, product, qty, product.getPrice());
            createdItems.add(oi);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        orderItems.saveAll(createdItems);

        BigDecimal shipping = shippingCost(subtotal);
        order.setSubtotal(subtotal);
        order.setShippingCost(shipping);
        order.setTotal(subtotal.add(shipping));

        payments.save(new Payment(order, order.getTotal()));

        notifications.orderCreated(user, order);

        return MapperUtils.orderToResponse(order, createdItems);
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> myOrders() {
        UUID userId = SecurityUtils.currentUserId();
        List<Order> list = orders.findByUserIdOrderByCreatedAtDesc(userId);

        List<OrderDTOs.OrderResponse> out = new ArrayList<>();
        for (Order o : list) {
            out.add(MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId())));
        }
        return out;
    }

    // ✅ NUOVO: lista globale paginata (solo STAFF/ADMIN)
    @Transactional(readOnly = true)
    public CommonDTOs.PageResponse<OrderDTOs.OrderResponse> listAllOrders(int page, int size) {
        if (!SecurityUtils.isStaffOrAdmin()) {
            throw new ApiExceptions.Forbidden("Forbidden");
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));

        PageRequest pr = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> p = orders.findAll(pr);

        List<OrderDTOs.OrderResponse> content = new ArrayList<>();
        for (Order o : p.getContent()) {
            content.add(MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId())));
        }

        return new CommonDTOs.PageResponse<>(
                content,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public OrderDTOs.OrderResponse getOrder(UUID id) {
        Order o;
        if (SecurityUtils.isStaffOrAdmin()) {
            o = orders.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Order not found"));
        } else {
            o = orders.findByIdAndUserId(id, SecurityUtils.currentUserId())
                    .orElseThrow(() -> new ApiExceptions.NotFound("Order not found"));
        }
        return MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId()));
    }

    @Transactional
    public OrderDTOs.OrderResponse updateStatus(UUID orderId, OrderStatus status) {
        if (!SecurityUtils.isStaffOrAdmin()) {
            throw new ApiExceptions.Forbidden("Forbidden");
        }

        Order o = orders.findById(orderId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Order not found"));

        o.setStatus(status);
        notifications.orderStatusChanged(o.getUser(), o);

        return MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId()));
    }

    @Transactional(readOnly = true)
    public List<TopSelling> topSelling(int limit) {
        var rows = orderItems.findTopSelling(PageRequest.of(0, Math.max(1, Math.min(limit, 50))));
        List<TopSelling> out = new ArrayList<>();

        for (var r : rows) {
            products.findById(r.getProductId())
                    .ifPresent(p -> out.add(new TopSelling(p.getId(), p.getName(), r.getQty())));
        }

        return out;
    }

    public record TopSelling(UUID productId, String name, long qty) {}

    private BigDecimal shippingCost(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(100)) >= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(9.99);
    }

    private String formatAddress(Address a) {
        return a.getStreet() + ", " + a.getPostalCode() + " " + a.getCity() + ", " + a.getCountry() + " (" + a.getType() + ")";
    }
}
