package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RequestLoanUseCaseTest {
    @Mock
    private RequestLoanRepositoryGateway gateway;
    @Mock
    private TypeLoanRepositoryGateway gateway2;
    private RequestLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new RequestLoanUseCase(gateway,gateway2);
    }

    @Test
    void shouldRegisterLoanWithDefaultStatus() {
        // Arrange
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(1L)
                .build();

        RequestLoanDomain expected = input.toBuilder()
                .statusId(1L)
                .build();
        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway.save(any())).thenReturn(Mono.just(expected));

        // Act & Assert
        StepVerifier.create(useCase.apply(input))
                .expectNextMatches(result -> result.getStatusId().equals(1L))
                .verifyComplete();
    }

    @Test
    void shouldThrowErrorWhenLoanTypeDoesNotExist() {
        // Arrange
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(99L)
                .build();

        when(gateway2.existsById(99L)).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(useCase.apply(input))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Tipo de prestamo no valido"))
                .verify();
    }


}
