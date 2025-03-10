package com.reactivespring.exceptionHandler;

import com.reactivespring.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {
    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleRequesBodyError(WebExchangeBindException ex){
        log.error("Exception caught in handleRequestBodyError: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}