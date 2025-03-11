package com.reactivespring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.movieInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/review"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    private final String MOVIES_URL = "/v1/movies";

    @DisplayName("WireMock Retriev Movie by ID")
    @Test
    void retrieveMovieById() {
        //given
        var movieId = "abc";
        //urlEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        //urlPathEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        var movie =  webTestClient.get()
                .uri(MOVIES_URL+"/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .returnResult().getResponseBody();

        Assertions.assertTrue(Objects.requireNonNull(movie).getReviewList().size()==2, "Wire Mock should return two reviews");
    }
    @DisplayName("WireMock Retriev Movie by ID 404")
    @Test
    void retrieveMovieById_whenMovieNotFound_404() {
        //given
        var movieId = "abc";
        //urlEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(WireMock.aResponse().withStatus(404)));

        //urlPathEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri(MOVIES_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var error = stringEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("There is no MovieInfo available for the passed Id: abc", error);
                });
                // Achtung: gibt true oder false zurück, Assertion fehlt .returnResult().getResponseBody().equals("There is no MovieInfo available for the passed Id: abc");
                // Achtung: gibt true oder false zurück, Assertion fehlt .isEqualTo("There is no MovieInfo available for the passed Id: abc");

    }

    @DisplayName("WireMock Retriev Movie by ID Review404")
    @Test
    void retrieveMovieById_whenMovieNotFound_Review404() {
        //given
        var movieId = "abc";
        //urlEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        //urlPathEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withStatus(404)));

        webTestClient.get()
                .uri(MOVIES_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(movie);
                    Assertions.assertTrue(movie.getReviewList().size()==0, "Wire Mock should return 0 reviews");
                    Assertions.assertEquals("Batman Begins", movie.getMovieInfo().getName(), "Name should be equal");
                });
    }

    @DisplayName("WireMock Retriev Movie by ID 500")
    @Test
    void retrieveMovieById_when_500() {
        //given
        var movieId = "abc";
        //urlEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(WireMock.aResponse().withStatus(500).withBody("MovieInfo Service Unavailable")));

        //urlPathEqualTo
//        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
//                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
//                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri(MOVIES_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var error = stringEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("ServerException in MoviesInfoService: MovieInfo Service Unavailable", error);
                });
    }

    @DisplayName("WireMock Retriev Movie by ID Review 500")
    @Test
    void retrieveMovieById_when_Review500() {
        //given
        var movieId = "abc";
        //urlEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        //urlPathEqualTo
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withStatus(500).withBody("MovieInfo Service Unavailable")));

        webTestClient.get()
                .uri(MOVIES_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var error = stringEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("ServerException in ReviewsService: MovieInfo Service Unavailable", error);
                });
    }
}
