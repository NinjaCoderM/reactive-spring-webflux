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

    public ReviewHandler(ReviewReactiveRepository repo, ReviewReactiveRepository reviewReactiveRepository) {
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
}
