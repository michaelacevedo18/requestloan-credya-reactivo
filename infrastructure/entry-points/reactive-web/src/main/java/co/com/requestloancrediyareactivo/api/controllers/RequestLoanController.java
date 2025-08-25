package co.com.requestloancrediyareactivo.api.controllers;
import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import co.com.requestloancrediyareactivo.api.mapper.RequestLoanMapper;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Creado exitosamente",
                content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de colicitud de prestamo no validos"),
        @ApiResponse(responseCode = "500", description = "Error interno")
})
@RestController
@RequestMapping(value = "/api/v1/solicitud", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class RequestLoanController {

    private final RequestLoanUseCase useCase;

    @PostMapping
    public Mono<ResponseEntity<ResponseDTO<RequestLoanDomain>>> create(@Valid @RequestBody Mono<RequestLoanCreateDTO> dtoMono) {
        return dtoMono
                .map(RequestLoanMapper::toDomain)
                .flatMap(useCase::apply)
                .map(user -> ResponseEntity.ok(
                        ResponseDTO.<RequestLoanDomain>builder()
                                .success(true)
                                .message("Solicitud registrada exitosamente")
                                .data(user)
                                .statusCode(200)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }
}
