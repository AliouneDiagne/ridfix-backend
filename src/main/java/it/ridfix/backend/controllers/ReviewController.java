package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.ReviewDTOs;
import it.ridfix.backend.services.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping
    public List<ReviewDTOs.ReviewResponse> list(@PathVariable UUID productId) {
        return reviews.listByProduct(productId);
    }

    @PostMapping
    public ReviewDTOs.ReviewResponse create(@PathVariable UUID productId, @Valid @RequestBody ReviewDTOs.ReviewCreateRequest req) {
        return reviews.addReview(productId, req);
    }
}
