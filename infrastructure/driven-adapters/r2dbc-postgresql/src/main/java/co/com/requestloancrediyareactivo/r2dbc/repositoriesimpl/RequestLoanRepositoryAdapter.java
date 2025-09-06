package co.com.requestloancrediyareactivo.r2dbc.repositoriesimpl;


import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import co.com.requestloancrediyareactivo.r2dbc.entities.RequestLoanEntity;
import co.com.requestloancrediyareactivo.r2dbc.helper.TransactionalUtils;

import co.com.requestloancrediyareactivo.r2dbc.repositories.RequestLoanRepository;
import co.com.requestloancrediyareactivo.r2dbc.repositories.TypeLoanRepository;
import lombok.RequiredArgsConstructor;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RequestLoanRepositoryAdapter implements RequestLoanRepositoryGateway {
    private final RequestLoanRepository repo1;
    private final TypeLoanRepository repo2;
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
    public Mono<Void> sendToqueaueStatus(RequestLoanDomain solicitud) {
        return sqsEventPublisherPort.sendStatusUpdateMessage(solicitud);
    }

    @Override
    public Mono<Double> sumMonthlyDebtByIdNumber(String idNumber) {
        return repo1.findByDocumentAndStatusId(idNumber, 2L)
                .map(RequestLoanEntity::getCalculatedMonthlyFee)   // BigDecimal?
                .filter(Objects::nonNull)
                .map(Double::doubleValue)
                .reduce(0.0, Double::sum)
                .defaultIfEmpty(0.0);
    }




    @Override
    public Mono<SendObjectToQueue> findByIdRelatedProps(UUID id) {
        return repo1.findById(id)                               // Mono<RequestLoanEntity>
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(e -> Mono.zip(
                        repo2.findById(e.getLoanTypeId())        // Mono<TypeLoanEntity>
                                .switchIfEmpty(Mono.error(new RuntimeException("Tipo no encontrado"))),
                        sumMonthlyDebtByIdNumber(e.getDocument())   // Mono<BigDecimal>
                                .defaultIfEmpty(0D)
                ).map(t -> {
                    var type = t.getT1();
                    var debt = t.getT2();

                    var out = mapper.map(e, SendObjectToQueue.class);
                    out.setLoanTypeInterestrate(type.getInterestrate());
                    out.setAuto_validation(type.getAuto_validation());
                    out.setLoanTypeName(type.getName());
                    out.setAvailableDebtCapacity(debt);
                    return out;
                }));
    }


}