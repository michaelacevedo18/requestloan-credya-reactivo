package co.com.requestloancrediyareactivo.api.controllers;
import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import co.com.requestloancrediyareactivo.api.mapper.RequestLoanMapper;
import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

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

    @PostMapping("/registrar")
    public Mono<ResponseEntity<ResponseDTO<RequestLoanDomain>>> create(
            @Valid @RequestBody Mono<RequestLoanCreateDTO> dtoMono,
            Authentication authentication
    ) {
        return dtoMono
                .map(RequestLoanMapper::toDomain)
                .flatMap(domain -> {
                    UserResponseDomain user = (UserResponseDomain) authentication.getPrincipal();
                    return useCase.apply(domain, user);
                })
                .map(data -> ResponseEntity.ok(
                        ResponseDTO.<RequestLoanDomain>builder()
                                .success(true)
                                .message("Solicitud registrada exitosamente")
                                .data(data)
                                .statusCode(200)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }




    @Operation(
            summary = "Listar solicitudes pendientes de revisión",
            description = "Este endpoint retorna un listado paginado de las solicitudes con estado pendiente de revisión para el usuario autenticado con rol ASESOR."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de paginación inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping("/pendientes")
    public Mono<ResponseDTO<PageDTO<RequestLoanDomain>>> getPendingRequests(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        List<Long> statuses = List.of(1L, 2L, 3L); // estados válidos

        return useCase.findPendingForReview(statuses, page, size)
                .map(data -> ResponseDTO.<PageDTO<RequestLoanDomain>>builder()
                        .success(true)
                        .message("Listado paginado de solicitudes pendientes")
                        .data(data)
                        .statusCode(200)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


/**
    //@GetMapping("/admin")
    //public Mono<ResponseEntity<String>> adminEndpoint() {
        //return Mono.just(ResponseEntity.ok("Acceso autorizado solo para rol admin"));
    }

    @GetMapping("/customer")
    public Mono<ResponseEntity<String>> customerEndpoint() {
        return Mono.just(ResponseEntity.ok("Acceso autorizado solo para rol customer"));
    }

    @GetMapping("/asesor")
    public Mono<ResponseEntity<String>> asesorEndpoint() {
        return Mono.just(ResponseEntity.ok("Acceso autorizado solo para rol asesor"));
    }
**/

}
