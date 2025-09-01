package co.com.requestloancrediyareactivo.r2dbc.repositories;

import co.com.requestloancrediyareactivo.r2dbc.entities.RequestLoanEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

// TODO: This file is just an example, you should delete or modify it
public interface RequestLoanRepository extends ReactiveCrudRepository<RequestLoanEntity, UUID>, ReactiveQueryByExampleExecutor<Object> {
    Flux<RequestLoanEntity> findAll();
}
