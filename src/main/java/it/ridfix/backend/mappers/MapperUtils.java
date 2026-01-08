package it.ridfix.backend.mappers;

import it.ridfix.backend.dto.AddressDTOs;
import it.ridfix.backend.dto.OrderDTOs;
import it.ridfix.backend.dto.ProductDTOs;
import it.ridfix.backend.dto.ReviewDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.entities.Address;
import it.ridfix.backend.entities.Order;
import it.ridfix.backend.entities.OrderItem;
import it.ridfix.backend.entities.Review;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.product.Accessory;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.entities.product.SparePart;

import java.math.BigDecimal;
import java.util.List;

public final class MapperUtils {
    private MapperUtils() {}

    public static UserDTOs.UserResponse userToResponse(User u) {
        return new UserDTOs.UserResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getSurname(),
                u.getRole(),
                u.getProfileImageUrl()
        );
    }

    public static AddressDTOs.AddressResponse addressToResponse(Address a) {
        return new AddressDTOs.AddressResponse(
                a.getId(),
                a.getType(),
                a.getStreet(),
                a.getCity(),
                a.getPostalCode(),
                a.getCountry(),
                a.isDefault()
        );
    }

    public static ProductDTOs.ProductResponse productToResponse(Product p, Double avgRating, long reviewCount) {
        String type;
        if (p instanceof SparePart) type = "SPARE_PART";
        else if (p instanceof Accessory) type = "ACCESSORY";
        else type = "PRODUCT";

        return new ProductDTOs.ProductResponse(
                p.getId(),
                type,
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQty(),
                p.isInStock(),
                p.isActive(),
                p.getImageUrl(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getBrand().getId(),
                p.getBrand().getName(),
                avgRating,
                reviewCount,
                p.getCreatedAt()
        );
    }

    public static OrderDTOs.OrderItemResponse orderItemToResponse(OrderItem oi) {
        BigDecimal lineTotal = oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
        return new OrderDTOs.OrderItemResponse(
                oi.getProduct().getId(),
                oi.getProduct().getName(),
                oi.getQuantity(),
                oi.getUnitPrice(),
                lineTotal
        );
    }

    public static OrderDTOs.OrderResponse orderToResponse(Order o, List<OrderItem> items) {
        List<OrderDTOs.OrderItemResponse> mapped = items.stream().map(MapperUtils::orderItemToResponse).toList();
        return new OrderDTOs.OrderResponse(
                o.getId(),
                o.getStatus(),
                o.getSubtotal(),
                o.getShippingCost(),
                o.getTotal(),
                o.getShippingAddressSnapshot(),
                o.getBillingAddressSnapshot(),
                mapped,
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    public static ReviewDTOs.ReviewResponse reviewToResponse(Review r) {
        return new ReviewDTOs.ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
