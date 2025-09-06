package co.com.requestloancrediyareactivo.model.requestloan.gateways;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CapacidadGateway {
    Mono<SendObjectToQueue> evaluar(SendObjectToQueue request);
    Mono<Double> getBaseSalary(String idNumber);
}

