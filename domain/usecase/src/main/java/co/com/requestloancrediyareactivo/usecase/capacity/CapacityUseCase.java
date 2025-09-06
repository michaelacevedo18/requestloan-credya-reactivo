package co.com.requestloancrediyareactivo.usecase.capacity;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.CapacidadGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts.ICapacidadUseCase;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CapacityUseCase implements ICapacidadUseCase {
    private final CapacidadGateway capacidadGateway;

    @Override
    public Mono<SendObjectToQueue> ejecutar(SendObjectToQueue request) {
        return capacidadGateway.evaluar(request);
    }

    @Override
    public Mono<Double> getBaseSalary(String idNumber) {
        return capacidadGateway.getBaseSalary(idNumber)
                .switchIfEmpty(Mono.error(new RuntimeException("Salario no encontrado para " + idNumber)));

    }
}
