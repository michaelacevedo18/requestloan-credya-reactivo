package co.com.requestloancrediyareactivo.model.requestloan.gateways;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import reactor.core.publisher.Mono;

public interface RequestLoanRepositoryGateway {
    Mono<RequestLoanDomain> save(RequestLoanDomain requestLoanD);
}
