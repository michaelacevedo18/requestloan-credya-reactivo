package co.com.requestloancrediyareactivo.model.requestloan.gateways;
import reactor.core.publisher.Mono;

public interface TypeLoanRepositoryGateway {
    Mono<Boolean> existsById(Long id);
}
