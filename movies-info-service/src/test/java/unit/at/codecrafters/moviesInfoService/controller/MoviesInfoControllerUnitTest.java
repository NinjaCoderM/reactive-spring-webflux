package at.codecrafters.moviesInfoService.controller;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import at.codecrafters.moviesInfoService.service.MovieInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest(controllers = MoviesInfoController.class)
//@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {
    @MockitoBean
    private MovieInfoService movieInfoService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Test addMovieInfo Unit Test")
    void testAddMovieInfo() {
        //given
        var mInfo = new MovieInfo(null, "xxxBatman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        //when
        Mockito.when(movieInfoService.addMovieInfo(Mockito.any(MovieInfo.class)))
                .thenReturn(Mono.just(mInfo));

        var respMovieInfo = webTestClient
                .post()
                .uri("/v1/movieinfos")
                .bodyValue(mInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(MovieInfo.class);
        //then
        StepVerifier.create(respMovieInfo.getResponseBody())
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo);
                    Assertions.assertEquals("xxxBatman Begins", movieInfo.getName(), "Name should match");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test addMovieInfo validation")
    void testAddMovieInfo_whenValidationExceptionThrown() {
        //given
        var mInfo = new MovieInfo(null, null, -2005, List.of(""), LocalDate.parse("2005-06-15"));
        //when

        webTestClient
                .post()
                .uri("/v1/movieinfos")
                .bodyValue(mInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var error = stringEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("movieInfo.cast must be present, movieInfo.name must be present, movieInfo.year must be a positive value", error);
                });
        //then

    }

    @DisplayName("findAll MovieInfo Unit Test")
    @Test
    void getAllMovieInfos() {
        //given
        var mInfo = new MovieInfo(null, "xxxBatman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        //when
        Mockito.when(movieInfoService.allMovieInfos())
                .thenReturn(Flux.just(mInfo));

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
                .expectNextCount(1)
                .verifyComplete();

    }

    @DisplayName("findById MovieInfo Unit Test")
    @Test
    void getMovieInfoById() {
        //given
        var id = "abc";
        //when
        Mockito.when(movieInfoService.findMovieInfoById(Mockito.any(String.class)))
                .thenReturn(Mono.just(new MovieInfo("abc", "Dark Knight Rises", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));
        //then
        webTestClient
                .get()
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");

    }

    @DisplayName("Update MovieInfo Unit Test")
    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var mInfo = new MovieInfo("abc","Dark Knight Rises 2", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        Mockito.when(movieInfoService.updateMovieInfo(Mockito.any(MovieInfo.class), Mockito.anyString()))
                .thenReturn(Mono.just(mInfo));

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

    @DisplayName("Delete MovieInfo Unit Test")
    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";
        Mockito.when(movieInfoService.deleteMovieInfo(Mockito.anyString()))
                .thenReturn(Mono.empty());
        //when
        webTestClient
                .delete()
                .uri("/v1/movieinfos/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .returnResult(Void.class);
    }


}
