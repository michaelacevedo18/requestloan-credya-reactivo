package co.com.requestloancrediyareactivo.api.routers;
import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import co.com.requestloancrediyareactivo.api.handlers.RequestLoanHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.ExampleObject;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class RequestLoanRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud/registrar",
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
                          "document": "1049049049",
                          "email": "cliente@correo.com",
                          "nombre": "Julian Perez",
                          "amount": 1500000,
                          "interesRate": 1.3,
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
            )
    })
    public RouterFunction<ServerResponse> requestLoanRoutes(RequestLoanHandler handler) {
        return route(POST("/api/v1/solicitud/registrar"), handler::create)
                .andRoute(POST("/api/v1/solicitud/pendientes"), handler::getPendingRequests);
    }
}