package at.codecrafters.moviesInfoService.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {
    @Test
    public void testSink() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux = replaySink.asFlux();

        integerFlux.subscribe( i -> {
            System.out.println("Ausgabe Wert: " + i);
        });

        Flux<Integer> integerFlux2 = replaySink.asFlux();

        integerFlux2.subscribe( i -> {
            System.out.println("Ausgabe Wert2: " + i);
        });

        replaySink.emitNext(14, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(15, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    public void testSink_multicast() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().multicast().onBackpressureBuffer();

        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux = replaySink.asFlux();

        integerFlux.subscribe( i -> {
            System.out.println("Ausgabe Wert: " + i);
        });

        Flux<Integer> integerFlux2 = replaySink.asFlux();

        integerFlux2.subscribe( i -> {
            System.out.println("Ausgabe Wert2: " + i);
        });

        replaySink.emitNext(14, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(15, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
