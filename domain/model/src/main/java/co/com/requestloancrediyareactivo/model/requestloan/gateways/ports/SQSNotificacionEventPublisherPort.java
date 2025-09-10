package co.com.requestloancrediyareactivo.model.requestloan.gateways.ports;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import reactor.core.publisher.Mono;

public interface SQSNotificacionEventPublisherPort {
   // Mono<Void> sendStatusUpdateMessage(RequestLoanDomain solicitud);
    Mono<Void> sendStatusUpdateMessage(SendObjectToQueue payload);
}
