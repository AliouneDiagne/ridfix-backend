package it.ridfix.backend.entities;

import it.ridfix.backend.entities.enums.InventoryMovementType;
import it.ridfix.backend.entities.product.Product;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
@EntityListeners(AuditingEntityListener.class)
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InventoryMovementType type;

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false, length = 120)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public InventoryMovement() {}

    public InventoryMovement(Product product, InventoryMovementType type, int qty, String reason, Order order) {
        this.product = product;
        this.type = type;
        this.qty = qty;
        this.reason = reason;
        this.order = order;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public InventoryMovementType getType() { return type; }
    public void setType(InventoryMovementType type) { this.type = type; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Instant getCreatedAt() { return createdAt; }
}
