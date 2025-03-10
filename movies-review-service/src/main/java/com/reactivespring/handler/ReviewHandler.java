package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private final ReviewReactiveRepository repo;

    public ReviewHandler(ReviewReactiveRepository repo) {
        this.repo = repo;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class).log()
                .doOnNext(this::validate)
                .flatMap(repo::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {

        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}" , constraintViolations);
        if(constraintViolations.size() > 0) {
            var errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw  new ReviewDataException(errorMessage);

        }

    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var moviesInfoId = request.queryParam("movieInfoId");
        if (moviesInfoId.isPresent()) {
            return repo.findByMovieInfoId(Long.valueOf(moviesInfoId.get())).collectList().flatMap(reviews -> ServerResponse.ok().body(Flux.fromIterable(reviews), Review.class));
        } else {
            var reviewsFlux = repo.findAll();
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var existingReview = repo.findById(request.pathVariable("id"))
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id: " + request.pathVariable("id"))));
        return existingReview.flatMap(review -> {
            return request.bodyToMono(Review.class).map(reqReview -> {
                review.setComment(reqReview.getComment());
                review.setRating(reqReview.getRating());
                return review;
            })
            .flatMap(repo::save)
            .flatMap(savedResponse-> ServerResponse.ok().body(Mono.just(savedResponse), Review.class));
        });
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var id = request.pathVariable("id");
        var existingReview = repo.findById(id);
        return existingReview
                .flatMap(review -> repo.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

}
