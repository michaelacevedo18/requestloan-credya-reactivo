package co.com.requestloancrediyareactivo.api.config;


import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
//import java.time.LocalDateTime;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**@Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Error interno del servidor";


        if (ex instanceof JwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Token inválido o expirado";
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = "Acceso denegado";
        }


        else if (ex instanceof ConstraintViolationException validationEx) {
            status = HttpStatus.BAD_REQUEST;
            message = validationEx.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Error de validación.");
        }


        else if (ex instanceof IllegalArgumentException || ex instanceof ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        }


        else if (ex instanceof ResponseStatusException statusEx) {
            status = (HttpStatus) statusEx.getStatusCode();
            message = statusEx.getReason() != null ? statusEx.getReason() : "Error HTTP";
        } else if (ex instanceof ResponseStatusException statusEx) {
            status = (HttpStatus) statusEx.getStatusCode();
            message = statusEx.getReason() != null ? statusEx.getReason() : "Error HTTP";
        }

        log.error("💥 [{}] {}", status.value(), message, ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ResponseDTO<String> response = ResponseDTO.<String>builder()
                .success(false)
                .statusCode(status.value())
                .message(message)
                //.timestamp(LocalDateTime.now())
                .build();

        try {
            String body = objectMapper.writeValueAsString(response);
            return exchange.getResponse().writeWith(Mono.just(
                    bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8))
            ));
        } catch (Exception e) {
            log.error("Error serializando JSON de error", e);
            return Mono.error(e);
        }
    }**/
        @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Error interno del servidor";

        if (ex instanceof JwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Token inválido o expirado";
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = "Acceso denegado";
        } else if (ex instanceof ConstraintViolationException validationEx) {
            status = HttpStatus.BAD_REQUEST;
            message = validationEx.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Error de validación.");
        } else if (ex instanceof IllegalArgumentException || ex instanceof ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        } else if (ex instanceof ResponseStatusException statusEx) {
            status = (HttpStatus) statusEx.getStatusCode();
            message = statusEx.getReason() != null ? statusEx.getReason() : "Error HTTP";
        }
        // ✅ Bloque adicional para RuntimeException personalizadas
        else if (ex instanceof RuntimeException) {
            status = HttpStatus.BAD_REQUEST; // O usa otro HttpStatus si aplica
            message = ex.getMessage() != null ? ex.getMessage() : "Excepción de ejecución inesperada";
        }

        log.error("💥 [{}] {}", status.value(), message, ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ResponseDTO<String> response = ResponseDTO.<String>builder()
                .success(false)
                .statusCode(status.value())
                .message(message)
                //.timestamp(LocalDateTime.now())
                .build();

        try {
            String body = objectMapper.writeValueAsString(response);
            return exchange.getResponse().writeWith(Mono.just(
                    bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8))
            ));
        } catch (Exception e) {
            log.error("Error serializando JSON de error", e);
            return Mono.error(e);
        }
    }

}
