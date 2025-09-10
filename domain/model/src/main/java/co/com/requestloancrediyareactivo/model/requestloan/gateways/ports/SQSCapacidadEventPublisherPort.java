package co.com.requestloancrediyareactivo.model.requestloan.gateways.ports;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import reactor.core.publisher.Mono;

public interface SQSCapacidadEventPublisherPort {
    Mono<Void> sendMessagetoqueue(SendObjectToQueue solicitud);
}
