package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan.capacity;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.CapacidadGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import co.com.requestloancrediyareactivo.usecase.capacity.CapacityUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CapacityUseCaseTest {

    @org.mockito.Mock CapacidadGateway capacidadGateway;

    private CapacityUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new CapacityUseCase(capacidadGateway);
    }

    private SendObjectToQueue sampleRequest(UUID id) {
        return SendObjectToQueue.builder()
                .id(id)
                .amount(3_000_000D)
                .term(36)
                .document("1049632878")
                .name("Michael")
                .email("michael@example.com")
                .statusId(1L)
                .loanTypeId(1L)
                .loanTypeInterestrate(12.0)
                .availableDebtCapacity(new BigDecimal("0"))
                .baseSalary(6_500_000.0)
                .build();
    }

    // ===== ejecutar() =====

    @Test
    void ejecutar_delegaEnGateway_yPropagaRespuesta() {
        UUID id = UUID.randomUUID();
        var req = sampleRequest(id);

        when(capacidadGateway.evaluar(any(SendObjectToQueue.class)))
                .thenReturn(Mono.just(req));

        StepVerifier.create(useCase.ejecutar(req))
                .expectNext(req)
                .verifyComplete();

        verify(capacidadGateway, times(1)).evaluar(req);
    }

    @Test
    void ejecutar_gatewayFalla_propagaError() {
        UUID id = UUID.randomUUID();
        var req = sampleRequest(id);

        when(capacidadGateway.evaluar(any(SendObjectToQueue.class)))
                .thenReturn(Mono.error(new RuntimeException("capacidad down")));

        StepVerifier.create(useCase.ejecutar(req))
                .expectErrorMatches(ex -> ex.getMessage().contains("capacidad down"))
                .verify();

        verify(capacidadGateway, times(1)).evaluar(req);
    }

    // ===== getBaseSalary() =====

    @Test
    void getBaseSalary_ok_devuelveValor() {
        String idNumber = "1049632878";

        when(capacidadGateway.getBaseSalary(idNumber))
                .thenReturn(Mono.just(6_500_000.0));

        StepVerifier.create(useCase.getBaseSalary(idNumber))
                .expectNext(6_500_000.0)
                .verifyComplete();

        verify(capacidadGateway, times(1)).getBaseSalary(idNumber);
    }

    @Test
    void getBaseSalary_empty_lanzaErrorConMensaje() {
        String idNumber = "9999999999";

        when(capacidadGateway.getBaseSalary(idNumber))
                .thenReturn(Mono.empty()); // dispara el switchIfEmpty(...)

        StepVerifier.create(useCase.getBaseSalary(idNumber))
                .expectErrorMatches(ex ->
                        ex.getMessage().contains("Salario no encontrado para " + idNumber))
                .verify();

        verify(capacidadGateway, times(1)).getBaseSalary(idNumber);
    }
}
