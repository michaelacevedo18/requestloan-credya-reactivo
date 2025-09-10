package co.com.requestloancrediyareactivo.sqs.listener;


import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final ObjectMapper mapper;
    private final IRequestLoanUseCase useCase;

@Override
public Mono<Void> apply(Message msg) {
    log.info("Recibiendo desde SQS---MMICHAEL--- [SQS] RAW body={}", msg.body()); 

    return Mono.fromCallable(() -> mapper.readValue(msg.body(), SendObjectToQueue.class))
            .doOnNext(evt -> log.info(
                    "[SQS] Evento parseado id={}, amount={}, statusId={}, calculatedMonthlyFee={}",
                    evt.getId(), evt.getAmount(), evt.getStatusId(), evt.getCalculatedMonthlyFee()
            ))
            .flatMap(evt -> {
                UUID id = evt.getId();
                int status = evt.getStatusId() != null ? evt.getStatusId().intValue() : 0;
                double cuota = evt.getCalculatedMonthlyFee() != null ? evt.getCalculatedMonthlyFee() : 0d;
                return useCase.updateStatusAtomQuot(id, status, 0, cuota).then();
            })
            .doOnSuccess(v -> log.debug("[SQS] processed messageId={} attrs={}", msg.messageId(), msg.attributesAsStrings()))
            .doOnError(e -> log.error("[SQS] processing error id={}, body={}", msg.messageId(), msg.body(), e))
            .onErrorResume(e -> Mono.empty());
}

}
