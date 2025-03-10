package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebTestClient
public class ReviewRouterTest {

    @ServiceConnection
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.5"));

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static  String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("abc", 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {

        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview!=null;
                    assert savedReview.getReviewId()!=null;
                });

        //then
    }

    @DisplayName("findAll Reviews Intg Test GET Endpoint")
    @Test
    void getAllReviews() {
        //given
        //when
        var respReview = webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class);
        //then
        StepVerifier.create(respReview.getResponseBody())
                .expectNextCount(3)
                .verifyComplete();

    }

    @DisplayName("findByMovieInfoId Review Intg Test GET Endpoint queryParam movieInfoId")
    @Test
    void getReviewByMovieInfoId() {
        //given
        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", "1")
                .buildAndExpand().toUri();
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

    @DisplayName("Update Review Intg Test PUT Endpoint")
    @Test
    void updateReview() {
        //given
        var id = "abc";
        var reviewUpdate = new Review("abc", 2L, "Excellent Movie+", 8.8);
        //when
        var respReview = webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", id)
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class);

        //then
        StepVerifier.create(respReview.getResponseBody())
                .assertNext(review -> {
                    assertNotNull(review);
                    Assertions.assertEquals(reviewUpdate.getComment(), review.getComment(), "Name should match");
                })
                .verifyComplete();

    }

    @DisplayName("Update Review Intg Test PUT Endpoint")
    @Test
    void updateReview_whenIdNotFound() {
        //given
        var id = "xyz";
        var reviewUpdate = new Review("abc", 2L, "Excellent Movie+", 8.8);
        //when
        var respReview = webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", id)
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Review not found for the given Review id: xyz");

    }

    @DisplayName("Delete Review Intg Test Delete Endpoint")
    @Test
    void deleteReview() {
        //given
        var id = "abc";
        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .returnResult(Void.class)
                .getResponseBody()
                .blockLast();

        var respReview = webTestClient
                .get()
                .uri(REVIEWS_URL)
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
