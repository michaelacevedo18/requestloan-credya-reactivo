package co.com.requestloancrediyareactivo.usecase.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class RequestLoanUseCase implements IRequestLoanUseCase {
    private final RequestLoanRepositoryGateway gateway1;
    private final TypeLoanRepositoryGateway gateway2;
    @Override
    public Mono<RequestLoanDomain> apply(RequestLoanDomain domain, UserResponseDomain user) {
        if (!domain.getDocument().equals(user.getIdNumber())) {
            return Mono.error(new RuntimeException("No autorizado: solo puedes crear solicitudes con tu propio documento"));
        }
        return gateway2.existsById(domain.getLoanTypeId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Tipo de prestamo no valido"));
                    }
                    RequestLoanDomain enriched = domain.toBuilder()
                            .statusId(1L)
                            .build();
                    return gateway1.save(enriched);
                });
    }

    @Override
    public Mono<PageDTO<RequestLoanDomain>> findPendingForReview(List<Long> statuses, int page, int size) {
        int skip = (page - 1) * size;

        Mono<List<RequestLoanDomain>> contentMono = gateway1.findAll()
                .filter(entity -> statuses.contains(entity.getStatusId()))
                .skip(skip)
                .take(size)
                .flatMap(domain ->
                        gateway2.findTypeById(domain.getLoanTypeId())
                                .map(type -> domain.toBuilder()
                                        .loanTypeName(type.getName())
                                        .build())
                )
                .collectList();


        Mono<Long> totalMono = gateway1.countByStatuses(statuses);

        return Mono.zip(contentMono, totalMono)
                .map(tuple -> {
                    List<RequestLoanDomain> content = tuple.getT1();
                    Long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / size);
                    return PageDTO.<RequestLoanDomain>builder()
                            .content(content)
                            .page(page)
                            .size(size)
                            .totalElements(total)
                            .totalPages(totalPages)
                            .build();
                });
    }


}
