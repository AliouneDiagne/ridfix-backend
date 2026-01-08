package it.ridfix.backend.services;

import it.ridfix.backend.dto.OrderDTOs;
import it.ridfix.backend.entities.*;
import it.ridfix.backend.entities.enums.InventoryMovementType;
import it.ridfix.backend.entities.enums.OrderStatus;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.*;
import it.ridfix.backend.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final ProductRepository products;
    private final AddressRepository addresses;
    private final UserRepository users;
    private final InventoryMovementRepository movements;
    private final PaymentRepository payments;
    private final NotificationService notifications;

    public OrderService(OrderRepository orders,
                        OrderItemRepository orderItems,
                        ProductRepository products,
                        AddressRepository addresses,
                        UserRepository users,
                        InventoryMovementRepository movements,
                        PaymentRepository payments,
                        NotificationService notifications) {
        this.orders = orders;
        this.orderItems = orderItems;
        this.products = products;
        this.addresses = addresses;
        this.users = users;
        this.movements = movements;
        this.payments = payments;
        this.notifications = notifications;
    }

    @Transactional
    public OrderDTOs.OrderResponse createOrder(OrderDTOs.CreateOrderRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        User user = users.findById(userId).orElseThrow(() -> new ApiExceptions.NotFound("User not found"));

        Address ship = addresses.findByIdAndUserId(req.shippingAddressId(), userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Shipping address not found"));
        Address bill = addresses.findByIdAndUserId(req.billingAddressId(), userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Billing address not found"));

        Order order = new Order(user, formatAddress(ship), formatAddress(bill));
        orders.save(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> createdItems = new ArrayList<>();

        for (OrderDTOs.OrderItemRequest item : req.items()) {
            Product product = products.findForUpdate(item.productId())
                    .orElseThrow(() -> new ApiExceptions.NotFound("Product not found: " + item.productId()));

            if (!product.isActive()) {
                throw new ApiExceptions.BadRequest("Product not available: " + product.getName());
            }

            int qty = item.quantity();
            if (qty <= 0) throw new ApiExceptions.BadRequest("Quantity must be >= 1");

            if (product.getStockQty() < qty) {
                throw new ApiExceptions.BadRequest("Not enough stock for " + product.getName());
            }

            product.setStockQty(product.getStockQty() - qty);

            movements.save(new InventoryMovement(
                    product,
                    InventoryMovementType.OUT,
                    qty,
                    "Order created",
                    order
            ));

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

        // Fail-soft email
        notifications.orderCreated(user, order);

        return MapperUtils.orderToResponse(order, createdItems);
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> myOrders() {
        UUID userId = SecurityUtils.currentUserId();
        List<Order> list = orders.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(o -> MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId()))).toList();
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
        Order o = orders.findById(orderId).orElseThrow(() -> new ApiExceptions.NotFound("Order not found"));
        o.setStatus(status);

        // notify user (fail-soft)
        User u = o.getUser();
        notifications.orderStatusChanged(u, o);

        return MapperUtils.orderToResponse(o, orderItems.findByOrderId(o.getId()));
    }

    @Transactional(readOnly = true)
    public List<TopSelling> topSelling(int limit) {
        var rows = orderItems.findTopSelling(PageRequest.of(0, Math.max(1, Math.min(limit, 50))));
        List<TopSelling> out = new ArrayList<>();
        for (var r : rows) {
            Product p = products.findById(r.getProductId()).orElse(null);
            if (p != null) {
                out.add(new TopSelling(p.getId(), p.getName(), r.getQty()));
            }
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
