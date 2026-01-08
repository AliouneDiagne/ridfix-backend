package it.ridfix.backend.specs;

import it.ridfix.backend.entities.product.Product;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> build(String q,
                                               UUID categoryId,
                                               UUID brandId,
                                               BigDecimal minPrice,
                                               BigDecimal maxPrice,
                                               Boolean inStock,
                                               Boolean activeOnly) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (inStock != null) {
                if (inStock) predicates.add(cb.greaterThan(root.get("stockQty"), 0));
                else predicates.add(cb.equal(root.get("stockQty"), 0));
            }
            if (activeOnly != null && activeOnly) {
                predicates.add(cb.isTrue(root.get("active")));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
