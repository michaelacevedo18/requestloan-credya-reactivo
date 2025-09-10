package co.com.requestloancrediyareactivo.sqs.listener.helper;

import co.com.requestloancrediyareactivo.sqs.listener.config.SQSProperties;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@Builder
public class SQSListener {
    private final SqsAsyncClient client;
    private final SQSProperties properties;
    private final Function<Message, Mono<Void>> processor;

    private Disposable subscription;

    public SQSListener start() {
        System.out.println("------Listener 1------");
        ReceiveMessageRequest base = ReceiveMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .maxNumberOfMessages(properties.maxNumberOfMessages())
                .waitTimeSeconds(properties.waitTimeSeconds())
                .visibilityTimeout(properties.visibilityTimeoutSeconds())
                .build();

        subscription =
                Mono.defer(() -> Mono.fromFuture(client.receiveMessage(base)))
                        .repeatWhen(r -> r.delayElements(Duration.ofMillis(properties.pollIntervalMs())))
                        .map(ReceiveMessageResponse::messages)
                        .filter(list -> list != null && !list.isEmpty())
                        .flatMapIterable(list -> list)
                        .concatMap(this::processOne)     // procesa 1 por 1, ordenado
                        .onErrorContinue((e, o) -> log.error("[SQS] polling error", e))
                        .subscribe();

        log.info("[SQS] listener started on {}", properties.queueUrl());
        return this;
    }


    private Mono<Void> processOne(Message m) {
        System.out.println("------Listener 2------");
        return processor.apply(m)
                .then(Mono.fromFuture(client.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(properties.queueUrl())
                        .receiptHandle(m.receiptHandle())
                        .build())))
                .doOnError(e -> log.error("[SQS] processing error id={}, keeping for retry", m.messageId(), e))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    public void stop() {
        if (subscription != null) subscription.dispose();
    }
}