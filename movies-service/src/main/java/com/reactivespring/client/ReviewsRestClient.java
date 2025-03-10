package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

@Component
public class ReviewsRestClient {
    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {
        var url = reviewsUrl;

        URI uri = UriComponentsBuilder
                .fromUriString(url)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toUri();;

        return webClient.get().uri(uri)
                .retrieve()
                .bodyToFlux(Review.class);
    }

}
