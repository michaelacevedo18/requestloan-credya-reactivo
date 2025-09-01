package co.com.requestloancrediyareactivo.r2dbc.adapters;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.SQSEventPublisherPort;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;



@Component
public class SQSEventPublisherAdapter implements SQSEventPublisherPort {
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SQSEventPublisherAdapter(
            SqsAsyncClient sqsAsyncClient,
            ObjectMapper objectMapper,
            @Value("${aws.queue}") String queueUrl) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    @Override
    public Mono<Void> sendStatusUpdateMessage(RequestLoanDomain solicitud) {
        return Mono.fromFuture(() -> {
                    try {
                        String payload = objectMapper.writeValueAsString(solicitud);

                        SendMessageRequest messageRequest = SendMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .messageBody(payload)
                                .build();

                        return sqsAsyncClient.sendMessage(messageRequest);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al enviar mensaje a SQS", e);
                    }
                }).doOnSuccess(resp -> System.out.println("Mensaje enviado a SQS con ID: " + resp.messageId()))
                .then();
    }

}