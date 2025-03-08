package at.codecrafters.moviesInfoService.controller;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoviesInfoControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @ServiceConnection
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.5"));

    @DisplayName("Add Movie Controller Test")
    @Test
    void addMovieInfo() {
        //given
        var mInfo = new MovieInfo(null, "xxxBatman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        //when
        var respMovieInfo = webTestClient
                .post()
                .uri("/v1/movieinfos")
                .bodyValue(mInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(MovieInfo.class);
        // ohne returnResult
        // auch  .expectBody(MovieInfo.class)
        // auch  .consumeWith ( respMovieInfo -> {
        //         var savedMovie = respMovieInfo.getResponseBody();
        //         Assertions.assertEquals("xxxBatman Begins", savedMovie.getName(), "Name should match");
        //  })

        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                        .assertNext(movieInfo -> {
                            assertNotNull(movieInfo);
                            Assertions.assertEquals("xxxBatman Begins", movieInfo.getName(), "Name should match");
                        })
                .verifyComplete();

    }
}