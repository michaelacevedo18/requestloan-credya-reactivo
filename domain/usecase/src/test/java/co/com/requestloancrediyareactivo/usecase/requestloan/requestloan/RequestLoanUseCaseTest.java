package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
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
    private UserResponseDomain user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new RequestLoanUseCase(gateway, gateway2);
        user = UserResponseDomain.builder()
                .idNumber("123456")
                .email("cliente@correo.com")
                .rolName("CUSTOMER")
                .build();
    }

    @Test
    void shouldRegisterLoanWithDefaultStatus() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(1L)
                .document("123456")
                .build();

        RequestLoanDomain expected = input.toBuilder().statusId(1L).build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway.save(any())).thenReturn(Mono.just(expected));

        StepVerifier.create(useCase.apply(input, user))
                .expectNextMatches(result -> result.getStatusId().equals(1L))
                .verifyComplete();
    }

    @Test
    void shouldThrowErrorWhenDocumentMismatch() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(1L)
                .document("000000")
                .build();

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("No autorizado"))
                .verify();
    }

    @Test
    void shouldThrowErrorWhenLoanTypeDoesNotExist() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(99L)
                .document("123456")
                .build();

        when(gateway2.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Tipo de prestamo no valido"))
                .verify();
    }

    @Test
    void shouldHandleErrorWhenExistsFails() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(1L)
                .document("123456")
                .build();

        when(gateway2.existsById(1L)).thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("DB Error"))
                .verify();
    }

    @Test
    void shouldHandleErrorWhenSaveFails() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0)
                .term(12)
                .email("cliente@correo.com")
                .loanTypeId(1L)
                .document("123456")
                .build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway.save(any())).thenReturn(Mono.error(new RuntimeException("Save failed")));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Save failed"))
                .verify();
    }
}
