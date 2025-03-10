package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {
    private final ReviewReactiveRepository repo;

    public ReviewHandler(ReviewReactiveRepository repo) {
        this.repo = repo;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
       return request.bodyToMono(Review.class)
                .flatMap(repo::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var reviewsFlux = repo.findAll();
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var existingReview = repo.findById(request.pathVariable("id"));
        return existingReview.flatMap(review -> {
            return request.bodyToMono(Review.class).map(reqReview -> {
                review.setComment(reqReview.getComment());
                review.setRating(reqReview.getRating());
                return review;
            })
            .flatMap(repo::save)
            .flatMap(savedResponse-> ServerResponse.ok().body(savedResponse, Review.class));
        });
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var id = request.pathVariable("id");
        var existingReview = repo.findById(id);
        return existingReview
                .flatMap(review -> repo.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsById(ServerRequest request) {
        var id = request.pathVariable("id");
        return repo.findById(id).flatMap(review -> ServerResponse.ok().body(review, Review.class));
    }
}
