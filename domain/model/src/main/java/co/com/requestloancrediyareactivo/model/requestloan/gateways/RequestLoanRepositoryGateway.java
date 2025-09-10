package co.com.requestloancrediyareactivo.model.requestloan.gateways;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RequestLoanRepositoryGateway {
    Mono<RequestLoanDomain> save(RequestLoanDomain requestLoanD);
    Mono<RequestLoanDomain> update(RequestLoanDomain requestLoanD);
    Flux<RequestLoanDomain> findAll();
    Mono<Long> countByStatuses(List<Long> statuses);
    Mono<RequestLoanDomain> findById(UUID id);
    Mono<Void> sendToqueaueStatus(SendObjectToQueue solicitud);
    Mono<Void> sendToQueaueCapacidad(SendObjectToQueue solicitud);
    Mono<BigDecimal> sumMonthlyDebtByIdNumber(String idNumber);
    Mono<SendObjectToQueue> findByIdRelatedProps(UUID id);
}

