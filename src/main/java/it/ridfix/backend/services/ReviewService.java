package it.ridfix.backend.services;

import it.ridfix.backend.dto.ReviewDTOs;
import it.ridfix.backend.entities.Review;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.OrderItemRepository;
import it.ridfix.backend.repositories.ProductRepository;
import it.ridfix.backend.repositories.ReviewRepository;
import it.ridfix.backend.repositories.UserRepository;
import it.ridfix.backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final UserRepository users;
    private final OrderItemRepository orderItems;

    public ReviewService(ReviewRepository reviews,
                         ProductRepository products,
                         UserRepository users,
                         OrderItemRepository orderItems) {
        this.reviews = reviews;
        this.products = products;
        this.users = users;
        this.orderItems = orderItems;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTOs.ReviewResponse> listByProduct(UUID productId) {
        // 404 se il prodotto non esiste (evita liste “fantasma”)
        if (!products.existsById(productId)) {
            throw new ApiExceptions.NotFound("Product not found");
        }

        return reviews.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(MapperUtils::reviewToResponse)
                .toList();
    }

    @Transactional
    public ReviewDTOs.ReviewResponse addReview(UUID productId, ReviewDTOs.ReviewCreateRequest req) {
        UUID userId = SecurityUtils.currentUserId();

        // 1️⃣ prodotto deve esistere (404 pulito)
        Product p = products.findById(productId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));

        // 2️⃣ review duplicata → 409 (PRIMA di tutto)
        if (reviews.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new ApiExceptions.Conflict("You already reviewed this product");
        }

        // 3️⃣ acquisto verificato → 400
        boolean hasPurchased = orderItems.existsByOrderUserIdAndProductId(userId, productId);
        if (!hasPurchased) {
            throw new ApiExceptions.BadRequest("You can only review products you have purchased.");
        }

        // 4️⃣ user (difensivo)
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));

        Review r = new Review(u, p, req.rating(), req.comment());
        reviews.save(r);

        return MapperUtils.reviewToResponse(r);
    }
}
