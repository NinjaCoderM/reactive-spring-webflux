import com.reactivespring.domain.Review;
import com.reactivespring.exceptionHandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    //@MockitoBean
    //private ReviewHandler reviewHandler;

    @MockitoBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    static  String REVIEWS_URL = "/v1/reviews";

    @Test
    @DisplayName("Test addReview Unit Test")
    void testAddReview() {
        //given
        var reviewIn = new Review(null, 1L, "Awesome Movie+", 9.0);
        //when
        Mockito.when(reviewReactiveRepository.save(Mockito.any(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie+", 9.0)));

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

    @Test
    @DisplayName("Test addReview Unit Test when Data is not valid")
    void addReview_validation() {
        //given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        Mockito.when(reviewReactiveRepository.save(Mockito.isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId: must not be null,rating.negative : rating is negative and please pass a non-negative value");
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
        Mockito.when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

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

        Mockito.when(reviewReactiveRepository.findById(Mockito.any(String.class)))
                .thenReturn(Mono.just(reviewIn));

        Mockito.when(reviewReactiveRepository.save(Mockito.any(Review.class))).thenReturn(Mono.just(reviewIn));

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
        var reviewIn = new Review("abc", 1L, "Awesome Movie+", 9.9);

        Mockito.when(reviewReactiveRepository.findById(Mockito.any(String.class)))
                .thenReturn(Mono.just(reviewIn));
        Mockito.when(reviewReactiveRepository.deleteById(Mockito.any(String.class)))
                .thenReturn(Mono.empty());

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

        Mockito.when(reviewReactiveRepository.findByMovieInfoId(Mockito.anyLong())).thenReturn(Flux.fromIterable(reviewsList));
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
