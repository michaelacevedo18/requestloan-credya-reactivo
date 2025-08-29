package co.com.requestloancrediyareactivo.model.requestloan.gateways;
import co.com.requestloancrediyareactivo.model.requestloan.models.TypeLoanDomain;
import reactor.core.publisher.Mono;

public interface TypeLoanRepositoryGateway {
    Mono<Boolean> existsById(Long id);
    Mono<TypeLoanDomain> findTypeById(Long id);
}
