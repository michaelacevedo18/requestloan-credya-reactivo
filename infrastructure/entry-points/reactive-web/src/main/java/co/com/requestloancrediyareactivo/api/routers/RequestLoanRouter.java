package co.com.requestloancrediyareactivo.api.routers;
import co.com.requestloancrediyareactivo.api.dtos.RequestLoanUpdateDTO;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import co.com.requestloancrediyareactivo.api.handlers.RequestLoanHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class RequestLoanRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    beanClass = RequestLoanHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "registrarSolicitud",
                            summary = "Registrar solicitud (handler)",
                            description = "Registra una solicitud de préstamo",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = RequestLoanCreateDTO.class),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Ejemplo de solicitud",
                                                            value = """
                        {
                          "document": "10496328788",
                          "email": "michaelacevedoruiz48@gmail.com",
                          "name": "Michael Acevedo",
                          "amount": 15000000,                          
                          "term": 6,
                          "loanTypeId": 1
                        }
                        """
                                                    )
                                            }
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Creado exitosamente"),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                                    @ApiResponse(responseCode = "500", description = "Error interno")
                            }
                    )
            )
            ,
            @RouterOperation(
                    path = "/api/v1/solicitud/pendientes",
                    beanClass = RequestLoanHandler.class,
                    beanMethod = "getPendingRequests",
                    operation = @Operation(
                            summary = "Listar solicitudes pendientes",
                            description = "Endpoint funcional para listar solicitudes pendientes con paginación",
                            operationId = "listarSolicitudesPendientes",
                            parameters = {
                                    @Parameter(name = "page", description = "Numero de pagina", in = ParameterIn.QUERY, required = false, schema = @Schema(type = "integer", defaultValue = "1")),
                                    @Parameter(name = "size", description = "Tamanio de pagina", in = ParameterIn.QUERY, required = false, schema = @Schema(type = "integer", defaultValue = "10"))
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Listado obtenido",
                                            content = @Content(schema = @Schema(implementation = ResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Parámetros inválidos"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/update",
                    beanClass = RequestLoanHandler.class,
                    beanMethod = "updateStatus",
                    operation = @Operation(
                            operationId = "actualizarEstadoSolicitud",
                            summary = "Actualizar estado de solicitud (aprobado o rechazado)",
                            description = "Permite aprobar o rechazar una solicitud existente",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = RequestLoanUpdateDTO.class),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Ejemplo de actualización",
                                                            value = """
                            {
                              "id": "96942fc4-65b9-44da-b29e-a836fb6f4d32",
                              "statusId": 2,
                              "comment": "Aprobado por buen historial"
                            }
                        """
                                                    )
                                            }
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Actualizado exitosamente"),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                                    @ApiResponse(responseCode = "404", description = "No encontrado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno")
                            }
                    )
            ),

    })
    public RouterFunction<ServerResponse> requestLoanRoutes(RequestLoanHandler handler) {
        return route(POST("/api/v1/solicitud"), handler::create)
                .andRoute(POST("/api/v1/solicitud/pendientes"), handler::getPendingRequests)
                .andRoute(PUT("/api/v1/solicitud/update"), handler::updateStatus)
                ;
    }
}