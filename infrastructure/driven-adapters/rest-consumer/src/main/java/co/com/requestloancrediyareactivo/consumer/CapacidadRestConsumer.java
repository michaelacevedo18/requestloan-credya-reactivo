package co.com.requestloancrediyareactivo.consumer;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.CapacidadGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CapacidadRestConsumer implements CapacidadGateway {
    private final WebClient client;
    private final CapacidadProps props;

    @Override
    public Mono<SendObjectToQueue> evaluar(SendObjectToQueue request) {
        return client.post()
                .uri(props.getPaths().getCalcular())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        r -> r.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Error calcular: " + b))))
                .bodyToMono(SendObjectToQueue.class);
    }

    @Override
    public Mono<Double> getBaseSalary(String idNumber) {
        var url = props.getPaths().getSalary();
        System.out.println("[getBaseSalary] --> GET " + url + "?idNumber=" + idNumber);

        return client.get()
                .uri(uri -> uri.path(url).queryParam("idNumber", idNumber).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .flatMap(b -> Mono.error(new RuntimeException("Error salario: " + b))))
                .bodyToMono(Double.class) // <<----- CLAVE
                .doOnNext(s -> System.out.println("[getBaseSalary] salary=" + s))
                .doOnError(e -> System.out.println("[getBaseSalary] EXCEPTION: " + e.getMessage()));
    }





}