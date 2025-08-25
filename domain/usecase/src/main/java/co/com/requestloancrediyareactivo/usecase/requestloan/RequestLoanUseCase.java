package co.com.requestloancrediyareactivo.usecase.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RequestLoanUseCase implements IRequestLoanUseCase {
    private final RequestLoanRepositoryGateway gateway1;
    private final TypeLoanRepositoryGateway gateway2;
    @Override
    public Mono<RequestLoanDomain> apply(RequestLoanDomain input) {
        return gateway2.existsById(input.getLoanTypeId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Tipo de prestamo no valido"));
                    }
                    RequestLoanDomain enriched = input.toBuilder()
                            .statusId(1L)
                            .build();
                    return gateway1.save(enriched);
                });
    }
}
