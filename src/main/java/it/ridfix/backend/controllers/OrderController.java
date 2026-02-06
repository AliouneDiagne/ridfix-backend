package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.OrderDTOs;
import it.ridfix.backend.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public OrderDTOs.OrderResponse create(@Valid @RequestBody OrderDTOs.CreateOrderRequest req) {
        return orders.createOrder(req);
    }

    @GetMapping("/me")
    public List<OrderDTOs.OrderResponse> myOrders() {
        return orders.myOrders();
    }

    // OrderController.java
    @GetMapping("/stats/top-selling")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderService.TopSelling> topSelling(
            @RequestParam(defaultValue = "10") int limit) {
        return orders.topSelling(limit);
    }

    @GetMapping("/{id}")
    public OrderDTOs.OrderResponse get(@PathVariable UUID id) {
        return orders.getOrder(id);
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PatchMapping("/{id}/status")
    public OrderDTOs.OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody OrderDTOs.UpdateOrderStatusRequest req) {
        return orders.updateStatus(id, req.status());
    }
}
