package it.ridfix.backend.repositories;

import it.ridfix.backend.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);

    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);

    interface RatingAggRow {
        Double getAvgRating();
        Long getCount();
    }

    @Query("""
           select avg(r.rating) as avgRating, count(r.id) as count
           from Review r
           where r.product.id = :productId
           """)
    RatingAggRow ratingAgg(@Param("productId") UUID productId);

    // ✅ Bulk aggregation to avoid N+1 in product list
    interface RatingAggBulkRow {
        UUID getProductId();
        Double getAvgRating();
        Long getCount();
    }

    @Query("""
           select r.product.id as productId,
                  avg(r.rating) as avgRating,
                  count(r.id) as count
           from Review r
           where r.product.id in :productIds
           group by r.product.id
           """)
    List<RatingAggBulkRow> ratingAggBulk(@Param("productIds") List<UUID> productIds);
}
