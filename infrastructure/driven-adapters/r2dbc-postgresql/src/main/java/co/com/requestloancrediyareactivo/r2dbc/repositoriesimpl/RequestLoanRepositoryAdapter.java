package co.com.requestloancrediyareactivo.r2dbc.repositoriesimpl;


import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.r2dbc.entities.RequestLoanEntity;
import co.com.requestloancrediyareactivo.r2dbc.helper.TransactionalUtils;

import co.com.requestloancrediyareactivo.r2dbc.repositories.RequestLoanRepository;
import lombok.RequiredArgsConstructor;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RequestLoanRepositoryAdapter implements RequestLoanRepositoryGateway {
    private final RequestLoanRepository repo1;
    private final ObjectMapper mapper;
    private final TransactionalUtils tx;

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
}