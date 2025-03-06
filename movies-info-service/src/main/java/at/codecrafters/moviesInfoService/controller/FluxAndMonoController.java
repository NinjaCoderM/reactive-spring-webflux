package at.codecrafters.moviesInfoService.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path="/flux")
public class FluxAndMonoController {

    @GetMapping(path = "/welcome")
    public Flux<String> getWelcomeMessage() {
        // Test with curl http://localhost:8080/flux/welcome
        return Flux.just("Welcome to Flux And Mono! ", "Yes ", "Year ", "For Real ");
    }

}
