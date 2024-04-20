package com.ai.FlatServer.global.exceptions.handler;

import com.ai.FlatServer.global.exceptions.FlatException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(MalformedURLException.class)
    public ResponseEntity<ErrorResponse> handleMalformedURLException(MalformedURLException e) {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(FlatException.class)
    public ResponseEntity<ErrorResponse> handleFlatException(FlatException e) {

        ErrorResponse response = ErrorResponse.builder(e, e.getFlatErrorCode().getStatusCode(),
                e.getFlatErrorCode().getStatusMessage()).build();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
