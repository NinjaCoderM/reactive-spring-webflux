package at.codecrafters.moviesInfoService.controller;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import at.codecrafters.moviesInfoService.repository.MovieInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired private MovieInfoRepository movieInfoRepository;

    @ServiceConnection
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.5"));

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null,"The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc","Dark Knight Rises", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepository.saveAll(movieInfos)
                .blockLast(); // nur bei Tests erlaubt
    }

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

    @DisplayName("findAll MovieInfo Controller Test")
    @Test
    void getAllMovieInfos() {
        //given
        //when
        var respMovieInfo = webTestClient
                .get()
                .uri("/v1/movieinfos")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class);
        // ohne returnResult
        // auch  .expectBodyList(MovieInfo.class)
        // auch  .hasSize(3);

        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .expectNextCount(3)
                .verifyComplete();

    }

    @DisplayName("findByYear MovieInfo Intg Test")
    @Test
    void getAllMovieInfosByYear() {
        //given
        URI uri = UriComponentsBuilder.fromUriString("/v1/movieinfos")
                .queryParam("year", 2008)
                .buildAndExpand().toUri();
        //when
        var respMovieInfo = webTestClient
                .get()
                //.uri("/v1/movieinfos?year=2008")
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class);
        // ohne returnResult
        // auch  .expectBodyList(MovieInfo.class)
        // auch  .hasSize(3);

        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .expectNextCount(2)
                .verifyComplete();

    }

    @DisplayName("findById MovieInfo Controller Test")
    @Test
    void getMovieInfoById() {
        //given
        var id = "abc";
        //when
        var respMovieInfo = webTestClient
                .get()
                // auch .uri("/v1/movieinfos/"+id)
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class);
        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .assertNext(movieInfo -> Assertions.assertEquals("Dark Knight Rises", movieInfo.getName(), "should be equal"))
                .verifyComplete();

        //Testen mit jsonPath
        webTestClient
                .get()
                // auch .uri("/v1/movieinfos/"+id)
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");

    }

    @DisplayName("findById MovieInfo Intg Test Fail")
    @Test
    void getMovieInfoById_whenIdNotFound() {
        //given
        var id = "abcx";
        //when
        webTestClient
                .get()
                // auch .uri("/v1/movieinfos/"+id)
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @DisplayName("Update MovieInfo Controller Test")
    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var mInfo = new MovieInfo("abc","Dark Knight Rises 2", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        //when
        var respMovieInfo = webTestClient
                .put()
                .uri("/v1/movieinfos/{id}", id)
                .bodyValue(mInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class);

        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo);
                    Assertions.assertEquals("Dark Knight Rises 2", movieInfo.getName(), "Name should match");
                })
                .verifyComplete();

    }

    @DisplayName("Update MovieInfo Controller Test Fail")
    @Test
    void updateMovieInfo_whenIdNotFound() {
        //given
        var id = "abcx";
        var mInfo = new MovieInfo("abcx","Dark Knight Rises 2", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        //when
        webTestClient
                .put()
                .uri("/v1/movieinfos/{id}", id)
                .bodyValue(mInfo)
                .exchange()
                .expectStatus()
                .isNotFound();


    }

    @DisplayName("Delete MovieInfo Controller Test")
    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";
        //when
        webTestClient
                .delete()
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .returnResult(Void.class)
                .getResponseBody()
                .blockLast();

        webTestClient
                .get()
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

}