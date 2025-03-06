package at.codecrafters.moviesInfoService.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.time.Duration;


@RestController
@RequestMapping(path="/flux")
public class FluxAndMonoController {

    @GetMapping(path = "/welcome")
    public Flux<String> getWelcomeMessage() {
        // Test with curl http://localhost:8080/flux/welcome
        return Flux.just("Welcome to Flux And Mono! ", "Yes ", "Year ", "For Real ");
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getStream() {
        // Test with curl http://localhost:8080/flux/stream
        return Flux.range(1, 100).delayElements(Duration.ofSeconds(1)).log();
    }

}
