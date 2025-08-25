package co.com.requestloancrediyareactivo.usecase.typeloan.primaryPorts;
import reactor.core.publisher.Mono;


public interface ITypeLoanUseCase {
    Mono<Boolean> existsById(Long id);
}
