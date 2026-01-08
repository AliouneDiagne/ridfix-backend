package it.ridfix.backend.dto;

import it.ridfix.backend.entities.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDTOs {
    private OrderDTOs() {}

    public record OrderItemRequest(
            @NotNull UUID productId,
            @Min(1) int quantity
    ) {}

    public record CreateOrderRequest(
            @Valid @NotNull List<OrderItemRequest> items,
            @NotNull UUID shippingAddressId,
            @NotNull UUID billingAddressId
    ) {}

    public record OrderItemResponse(
            UUID productId,
            String name,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            UUID id,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal shippingCost,
            BigDecimal total,
            String shippingAddressSnapshot,
            String billingAddressSnapshot,
            List<OrderItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record UpdateOrderStatusRequest(
            @NotNull OrderStatus status
    ) {}
}
