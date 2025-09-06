package co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface ICapacidadUseCase {
    public Mono<SendObjectToQueue> ejecutar(SendObjectToQueue request);
    Mono<Double> getBaseSalary(String idNumber);
}
