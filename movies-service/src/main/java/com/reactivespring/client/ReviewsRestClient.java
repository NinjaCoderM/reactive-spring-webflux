package com.reactivespring.client;

import com.reactivespring.config.RetryUtil;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
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
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException("ServerException in ReviewsService: " + responseMessage)));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec(
                        ReviewsServerException.class,
                        retrySignal -> new ReviewsServerException("Max retries reached: " +
                                retrySignal.totalRetries() + " Cause: " + retrySignal.failure().getMessage())
                ));
    }

}
