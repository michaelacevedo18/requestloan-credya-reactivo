package co.com.requestloancrediyareactivo.api.config;


import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ResponseDTO>> handleValidationErrors(WebExchangeBindException ex, ServerWebExchange exchange) {
        List<String> details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
       
        ResponseDTO error = ResponseDTO.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Validacion fallida")
                .message("Uno o mas campos no cumplen con las validaciones")
                .timestamp(LocalDateTime.now())
                .details(details)
                .path(exchange.getRequest().getPath().value())
                .build();
        log.error("Error inesperado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ResponseDTO>> handleRuntime(RuntimeException ex, ServerWebExchange exchange) {
        ResponseDTO error = ResponseDTO.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Error en la peticion")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        log.error("Error inesperado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResponseDTO>> handleAll(Exception ex, ServerWebExchange exchange) {
        ResponseDTO error = ResponseDTO.builder()
                .success(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error interno del servidor")
                .message("Ha ocurrido un error inesperado")
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        log.error("Error inesperado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}