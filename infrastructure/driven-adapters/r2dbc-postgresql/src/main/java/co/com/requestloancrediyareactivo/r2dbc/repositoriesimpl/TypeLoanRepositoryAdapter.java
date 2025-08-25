package co.com.requestloancrediyareactivo.r2dbc.repositoriesimpl;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.r2dbc.helper.TransactionalUtils;
import co.com.requestloancrediyareactivo.r2dbc.repositories.TypeLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TypeLoanRepositoryAdapter implements TypeLoanRepositoryGateway {
    private final TypeLoanRepository repo;
    private final TransactionalUtils tx;

    @Override
    public Mono<Boolean> existsById(Long id) {
        return tx.execute(repo.existsById(id));
    }
}