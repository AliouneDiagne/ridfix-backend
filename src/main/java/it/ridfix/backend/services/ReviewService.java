package it.ridfix.backend.services;

import it.ridfix.backend.dto.ReviewDTOs;
import it.ridfix.backend.entities.Review;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.mappers.MapperUtils;
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

    public ReviewService(ReviewRepository reviews, ProductRepository products, UserRepository users) {
        this.reviews = reviews;
        this.products = products;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTOs.ReviewResponse> listByProduct(UUID productId) {
        return reviews.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(MapperUtils::reviewToResponse)
                .toList();
    }

    @Transactional
    public ReviewDTOs.ReviewResponse addReview(UUID productId, ReviewDTOs.ReviewCreateRequest req) {
        UUID userId = SecurityUtils.currentUserId();

        if (reviews.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new ApiExceptions.Conflict("You already reviewed this product");
        }

        User u = users.findById(userId).orElseThrow(() -> new ApiExceptions.NotFound("User not found"));
        Product p = products.findById(productId).orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));

        Review r = new Review(u, p, req.rating(), req.comment());
        reviews.save(r);
        return MapperUtils.reviewToResponse(r);
    }
}
