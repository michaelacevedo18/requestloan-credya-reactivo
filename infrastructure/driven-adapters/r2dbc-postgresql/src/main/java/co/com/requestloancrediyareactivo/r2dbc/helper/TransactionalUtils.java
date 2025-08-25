package co.com.requestloancrediyareactivo.r2dbc.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionalUtils {
    private final TransactionalOperator operator;

    public <T> Mono<T> execute(Mono<T> action) {
        return action.as(operator::transactional);
    }
}
