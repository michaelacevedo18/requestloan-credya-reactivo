package co.com.requestloancrediyareactivo.api.handlers;


import co.com.requestloancrediyareactivo.api.adapters.DTOValidatorAdapter;
import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.api.dtos.ResponseDTO;
import co.com.requestloancrediyareactivo.api.mapper.RequestLoanMapper;
import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestLoanHandler {

    private final RequestLoanUseCase useCase;
    private final DTOValidatorAdapter validatorAdapter;

    public Mono<ServerResponse> create(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (UserResponseDomain) securityContext.getAuthentication().getPrincipal())
                .flatMap(user ->
                        request.bodyToMono(RequestLoanCreateDTO.class)
                                .doOnNext(validatorAdapter::validateOrThrow)
                                .map(RequestLoanMapper::toDomain)
                                .flatMap(domain -> useCase.apply(domain, user))
                                .flatMap(data -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ResponseDTO.<RequestLoanDomain>builder()
                                                .success(true)
                                                .message("Solicitud registrada exitosamente")
                                                .data(data)
                                                .statusCode(200)
                                                .timestamp(LocalDateTime.now())
                                                .build()))
                );
    }

    public Mono<ServerResponse> getPendingRequests(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("1"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        List<Long> statuses = List.of(1L, 2L, 3L);

        return useCase.findPendingForReview(statuses, page, size)
                .flatMap(data -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseDTO.<PageDTO<RequestLoanDomain>>builder()
                                .success(true)
                                .message("Listado paginado de solicitudes pendientes")
                                .data(data)
                                .statusCode(200)
                                .timestamp(LocalDateTime.now())
                                .build()));
    }
}
