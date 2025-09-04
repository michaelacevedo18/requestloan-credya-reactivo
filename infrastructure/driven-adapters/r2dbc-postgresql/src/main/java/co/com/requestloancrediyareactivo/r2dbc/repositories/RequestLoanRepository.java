package co.com.requestloancrediyareactivo.r2dbc.repositories;

import java.util.UUID;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.requestloancrediyareactivo.r2dbc.entities.RequestLoanEntity;
import reactor.core.publisher.Flux;


public interface RequestLoanRepository extends ReactiveCrudRepository<RequestLoanEntity, UUID>, ReactiveQueryByExampleExecutor<Object> {
    Flux<RequestLoanEntity> findAll();
    Flux<RequestLoanEntity> findByDocumentAndStatusId(String document, Long statusId);
}
