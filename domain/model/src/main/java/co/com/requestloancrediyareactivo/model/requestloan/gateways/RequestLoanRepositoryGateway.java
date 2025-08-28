package co.com.requestloancrediyareactivo.model.requestloan.gateways;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RequestLoanRepositoryGateway {
    Mono<RequestLoanDomain> save(RequestLoanDomain requestLoanD);
    Flux<RequestLoanDomain> findAll();
    Mono<Long> countByStatuses(List<Long> statuses);
}
