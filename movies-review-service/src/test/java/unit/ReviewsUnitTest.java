import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    @MockitoBean
    private ReviewHandler reviewHandler;

    @Autowired
    private WebTestClient webTestClient;

    static  String REVIEWS_URL = "/v1/reviews";

    @Test
    @DisplayName("Test addReview Unit Test")
    void testAddMovieInfo() {
        //given
        var reviewIn = new Review(null, 1L, "Awesome Movie+", 9.0);
        //when
        Mockito.when(reviewHandler.addReview(Mockito.any(ServerRequest.class)))
                .thenReturn(ServerResponse.status(HttpStatus.CREATED).bodyValue(reviewIn));

        var respReview = webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(reviewIn)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(Review.class);
        //then
        StepVerifier.create(respReview.getResponseBody())
                .assertNext(review -> {
                    assertNotNull(review);
                    Assertions.assertEquals(reviewIn.getComment(), review.getComment(), "Comment should match");
                })
                .verifyComplete();
    }

    @DisplayName("findAll Reviews Unit Test")
    @Test
    void getAllReviews() {
        //given
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("abc", 2L, "Excellent Movie", 8.0));
        //when
        Mockito.when(reviewHandler.getReviews(Mockito.any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue(reviewsList));

        var respMovieInfo = webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class);

        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .expectNextCount(3)
                .verifyComplete();

    }

    @DisplayName("Update Review Unit Test")
    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var reviewIn = new Review("abc", 1L, "Awesome Movie+", 9.9);

        Mockito.when(reviewHandler.updateReview(Mockito.any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue(reviewIn));

        //when
        var respReview = webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", id)
                .bodyValue(reviewIn)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class);

        //then
        StepVerifier.create(respReview.getResponseBody())
                .assertNext(review -> {
                    assertNotNull(review);
                    Assertions.assertEquals(reviewIn.getComment(), review.getComment(), "Comment should match");
                })
                .verifyComplete();

    }

    @DisplayName("Delete Review Unit Test")
    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";
        Mockito.when(reviewHandler.deleteReview(Mockito.any(ServerRequest.class)))
                .thenReturn(ServerResponse.noContent().build());
        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .returnResult(Void.class);
    }

    @DisplayName("findByMovieInfoId Review Unit Test GET Endpoint queryParam movieInfoId")
    @Test
    void getReviewByMovieInfoId() {
        //given
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0));
        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", "1")
                .buildAndExpand().toUri();
        Mockito.when(reviewHandler.getReviews(Mockito.any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().body(Flux.fromIterable(reviewsList), Review.class));

        //when
        var respReview = webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class);
        //then
        StepVerifier.create(respReview.getResponseBody())
                .expectNextCount(2)
                .verifyComplete();
    }
}
