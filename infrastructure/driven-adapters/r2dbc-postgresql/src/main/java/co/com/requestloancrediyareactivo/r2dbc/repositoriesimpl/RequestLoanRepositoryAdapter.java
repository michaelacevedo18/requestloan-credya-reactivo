package co.com.requestloancrediyareactivo.r2dbc.repositoriesimpl;


import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSCapacidadEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSNotificacionEventPublisherPort;
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
    private final SQSNotificacionEventPublisherPort sqsEventPublisherPort;
    private final SQSCapacidadEventPublisherPort sqsCapacidadEventPublisherPort;
    private static final String LOGA = "[adapter.save]";

    @Override
    public Mono<RequestLoanDomain> save(RequestLoanDomain domain) {
        final String trace = java.util.UUID.randomUUID().toString().substring(0, 8);
        System.out.println(LOGA+"("+trace+") in domain="+domain);

        return tx.execute(
                Mono.justOrEmpty(domain)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Domain null en save")))
                        .map(d -> {
                            var e = mapper.map(d, RequestLoanEntity.class);
                            if (e == null) throw new IllegalStateException("mapper domain->entity devolvió null");
                            e.setId(null);
                            if (e.getVersion() == null) e.setVersion(1L);
                            return e;
                        })
                        .flatMap(e -> {
                            System.out.println(LOGA+"("+trace+") repo1.save");
                            return repo1.save(e);
                        })
                        .flatMap(saved -> {
                            var out = mapper.map(saved, RequestLoanDomain.class);
                            if (out == null) return Mono.error(new IllegalStateException("mapper entity->domain devolvió null"));
                            return Mono.just(out);
                        })
                        .doOnError(err -> System.out.println(LOGA+"("+trace+") ERROR "+err))
                        .doOnSuccess(ok -> System.out.println(LOGA+"("+trace+") OK id="+ok.getId()+" ver="+ok.getVersion()))
        );
    }


    @Override
    public Mono<RequestLoanDomain> update(RequestLoanDomain domain) {
        System.out.println("-------Cuota a act: "+domain.getCalculatedMonthlyFee());

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
    public Mono<Void> sendToqueaueStatus(SendObjectToQueue solicitud) {
        return sqsEventPublisherPort.sendStatusUpdateMessage(solicitud);
    }



    @Override
    public Mono<BigDecimal> sumMonthlyDebtByIdNumber(String idNumber) {
        return repo1.findByDocumentAndStatusId(idNumber, 2L)     // Flux<RequestLoanEntity>
                .map(RequestLoanEntity::getCalculatedMonthlyFee)      // Flux<BigDecimal>
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)             // Mono<BigDecimal>
                .map(bd -> bd.setScale(2, RoundingMode.HALF_UP))      // opcional, asegura (18,2)
                .doOnNext(total -> System.out.println("[sumDebt] total=" + total));
    }





    @Override
    public Mono<SendObjectToQueue> findByIdRelatedProps(UUID id) {
        return repo1.findById(id)                               // Mono<RequestLoanEntity>
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(e -> Mono.zip(
                        repo2.findById(e.getLoanTypeId())        // Mono<TypeLoanEntity>
                                .switchIfEmpty(Mono.error(new RuntimeException("Tipo no encontrado"))),
                        sumMonthlyDebtByIdNumber(e.getDocument())   // Mono<BigDecimal>
                                .defaultIfEmpty(BigDecimal.ZERO)
                ).map(t -> {
                    var type = t.getT1();
                    var debt = t.getT2();
                    BigDecimal fee = debt;
                    var out = mapper.map(e, SendObjectToQueue.class);
                    out.setLoanTypeInterestrate(type.getInterestrate());
                    out.setAuto_validation(type.getAuto_validation());
                    out.setLoanTypeName(type.getName());
                    out.setAvailableDebtCapacity(fee);
                    return out;
                }));
    }

    @Override
    public Mono<Void> sendToQueaueCapacidad(SendObjectToQueue solicitud) {
        return sqsCapacidadEventPublisherPort.sendMessagetoqueue(solicitud);
    }

}