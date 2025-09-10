package co.com.requestloancrediyareactivo.r2dbc.adapters;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSCapacidadEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSNotificacionEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


@Component
public class SQSCapacidadEventPublisherAdapter implements SQSCapacidadEventPublisherPort {
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SQSCapacidadEventPublisherAdapter(
            @Qualifier("publisherSqsClient")SqsAsyncClient sqsAsyncClient,
            ObjectMapper objectMapper,
            @Value("${aws.queue2}") String queueUrl) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    @Override
public Mono<Void> sendMessagetoqueue(SendObjectToQueue solicitud) {
    return Mono.fromFuture(() -> {
        try {
            String payload = objectMapper.writeValueAsString(solicitud);
            System.out.println("[SQS][OUT][Sale el mensaje a la cola CapacidadEndeudamiento] " + payload); // 👈 imprime el JSON

            SendMessageRequest messageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();

            return sqsAsyncClient.sendMessage(messageRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar mensaje a SQS", e);
        }
    })
    .doOnSuccess(resp -> System.out.println("[SQS] OK messageId=" + resp.messageId()))
    .doOnError(err -> System.out.println("[SQS] ERROR " + err.getMessage()))
    .then();
}


}