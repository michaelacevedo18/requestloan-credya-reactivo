package co.com.requestloancrediyareactivo.model.requestloan.gateways.ports;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import reactor.core.publisher.Mono;

public interface SQSEventPublisherPort {
    Mono<Void> sendStatusUpdateMessage(RequestLoanDomain solicitud);
}
