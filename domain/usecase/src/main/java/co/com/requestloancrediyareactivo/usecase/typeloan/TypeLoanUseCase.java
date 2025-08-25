package co.com.requestloancrediyareactivo.usecase.typeloan;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.usecase.typeloan.primaryPorts.ITypeLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TypeLoanUseCase implements ITypeLoanUseCase {
    private final TypeLoanRepositoryGateway gateway;

    @Override
    public Mono<Boolean> existsById(Long id) {
        return gateway.existsById(id);
    }
}
