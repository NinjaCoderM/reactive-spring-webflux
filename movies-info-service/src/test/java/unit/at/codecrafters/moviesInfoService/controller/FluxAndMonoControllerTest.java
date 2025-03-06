package at.codecrafters.moviesInfoService.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@WebFluxTest(FluxAndMonoController.class)
class FluxAndMonoControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void getWelcomeMessage_whenResponseIsOneClass_expectBody() {
        //given

        //when
        webClient.get()
                .uri("/flux/welcome")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Welcome to Flux And Mono! Yes Year For Real ");
        //then
    }

    @Test
    void getWelcomeMessage_getResponseBody_StepVerifier() {
        //given

        //when
        var resultFlux =  webClient.get()
                .uri("/flux/welcome")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(String.class).getResponseBody();

        //then
        StepVerifier.create(resultFlux)
                .expectNext("Welcome to Flux And Mono! ","Yes ","Year ","For Real ")
                .verifyComplete();
    }

    @Test
    void getWelcomeMessage_consumeWith() {
        //given

        //when
        webClient.get()
                .uri("/flux/welcome")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(String.class)
                .consumeWith(listEntityExchangeResult -> {
                    var result = listEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals(4,Objects.requireNonNull(result).size(), "Size should be 4" );
                    Assertions.assertTrue(result.contains("Welcome to Flux And Mono! "), "Result should contain: Welcome to Flux And Mono! ");
                });


    }

    @Test
    void getStream() {
    }
}