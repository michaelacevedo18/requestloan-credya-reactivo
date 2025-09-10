package co.com.requestloancrediyareactivo.model.requestloan.gateways;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import reactor.core.publisher.Mono;



public interface CapacidadGateway {
    Mono<SendObjectToQueue> evaluar(SendObjectToQueue request);
    Mono<Double> getBaseSalary(String idNumber);
}

