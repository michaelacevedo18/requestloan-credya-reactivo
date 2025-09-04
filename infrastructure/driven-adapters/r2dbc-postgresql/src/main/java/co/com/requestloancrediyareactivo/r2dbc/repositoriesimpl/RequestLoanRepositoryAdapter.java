package co.com.requestloancrediyareactivo.r2dbc.repositoriesimpl;


import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.r2dbc.entities.RequestLoanEntity;
import co.com.requestloancrediyareactivo.r2dbc.helper.TransactionalUtils;

import co.com.requestloancrediyareactivo.r2dbc.repositories.RequestLoanRepository;
import lombok.RequiredArgsConstructor;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RequestLoanRepositoryAdapter implements RequestLoanRepositoryGateway {
    private final RequestLoanRepository repo1;
    private final ObjectMapper mapper;
    private final TransactionalUtils tx;
    private final SQSEventPublisherPort sqsEventPublisherPort;

    @Override
    public Mono<RequestLoanDomain> save(RequestLoanDomain domain) {
        return tx.execute(
                Mono.fromSupplier(() -> {
                            var entity = mapper.map(domain, RequestLoanEntity.class);
                            entity.setId(null);
                            return entity;
                        })
                        .flatMap(repo1::save)
                        .map(saved -> mapper.map(saved, RequestLoanDomain.class))
        );
    }

    @Override
    public Mono<RequestLoanDomain> update(RequestLoanDomain domain) {
        return tx.execute(
                Mono.fromSupplier(() -> {
                            var entity = mapper.map(domain, RequestLoanEntity.class);
                            return entity;
                        })
                        .flatMap(repo1::save)
                        .map(saved -> mapper.map(saved, RequestLoanDomain.class))
        );
    }


    @Override
    public Flux<RequestLoanDomain> findAll() {
        return repo1.findAll()
                .map(entity -> mapper.map(entity, RequestLoanDomain.class));
    }

    @Override
    public Mono<Long> countByStatuses(List<Long> statuses) {
        return repo1.findAll()
                .filter(e -> statuses.contains(e.getStatusId()))
                .count();
    }

    @Override
    public Mono<RequestLoanDomain> findById(UUID id) {
        return repo1.findById(id)
                .map(entity -> mapper.map(entity, RequestLoanDomain.class));
    }



    @Override
    public Mono<Void> sendStatusUpdateMessage(RequestLoanDomain solicitud) {
        return sqsEventPublisherPort.sendStatusUpdateMessage(solicitud);
    }

    @Override
    public Mono<BigDecimal> sumMonthlyDebtByIdNumber(String idNumber) {
        return repo1.findByDocumentAndStatusId(idNumber, 2L)
                .map(RequestLoanEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO,
                        (acc, amt) -> acc.add(BigDecimal.valueOf(amt)))
                .map(total -> total.setScale(2, RoundingMode.HALF_UP))
                .defaultIfEmpty(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }





}