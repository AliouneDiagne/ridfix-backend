package it.ridfix.backend.repositories;

import it.ridfix.backend.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    // ✅ VERIFIED PURCHASE CHECK
    boolean existsByOrderUserIdAndProductId(UUID userId, UUID productId);

    interface TopSellingRow {
        UUID getProductId();
        Long getQty();
    }

    @Query("""
           select oi.product.id as productId, sum(oi.quantity) as qty
           from OrderItem oi
           group by oi.product.id
           order by sum(oi.quantity) desc
           """)
    List<TopSellingRow> findTopSelling(org.springframework.data.domain.Pageable pageable);
}
