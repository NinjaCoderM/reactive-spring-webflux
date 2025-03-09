package at.codecrafters.moviesInfoService.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequesBodyError(WebExchangeBindException ex){
        log.error("Exception caught in handleRequestBodyError: {}", ex.getMessage(), ex);
        var errorInf = ex.getBindingResult().getAllErrors().stream()
                .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Unknown error"))
                .sorted()
                .collect(Collectors.joining(", "));
        log.error("Error is: {}", errorInf);
        return ResponseEntity.badRequest().body(errorInf);
    }
}
