package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.CommonDTOs;
import it.ridfix.backend.dto.OrderDTOs;
import it.ridfix.backend.services.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public OrderDTOs.OrderResponse create(@Valid @RequestBody OrderDTOs.CreateOrderRequest req) {
        return orders.createOrder(req);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public List<OrderDTOs.OrderResponse> myOrders() {
        return orders.myOrders();
    }

    // STAFF/ADMIN (paginazione validata)
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public CommonDTOs.PageResponse<OrderDTOs.OrderResponse> listAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return orders.listAllOrders(page, size);
    }

    // ADMIN only (limit validato)
    @GetMapping("/stats/top-selling")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderService.TopSelling> topSelling(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return orders.topSelling(limit);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public OrderDTOs.OrderResponse get(@PathVariable UUID id) {
        return orders.getOrder(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public OrderDTOs.OrderResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderDTOs.UpdateOrderStatusRequest req
    ) {
        return orders.updateStatus(id, req.status());
    }
}
