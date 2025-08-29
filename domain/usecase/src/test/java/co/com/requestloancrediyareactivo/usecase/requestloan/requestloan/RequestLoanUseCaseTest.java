package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.TypeLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

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

    @Test
    void shouldReturnPagedResults() {
        List<Long> statuses = List.of(1L, 2L, 3L);
        int page = 1;
        int size = 6;
        int skip = (page - 1) * size;

        List<RequestLoanDomain> allFiltered = IntStream.range(0, 28)
                .mapToObj(i -> RequestLoanDomain.builder()
                        .statusId(1L)
                        .loanTypeId(1L)
                        .build())
                .toList();

        List<RequestLoanDomain> expectedPage = allFiltered.stream()
                .skip(skip)
                .limit(size)
                .map(loan -> loan.toBuilder().loanTypeName("Personal").build())
                .toList();

        when(gateway.findAll()).thenReturn(Flux.fromIterable(allFiltered));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just((long) allFiltered.size()));

        // Simular findTypeById para cada llamada
        when(gateway2.findTypeById(1L)).thenAnswer(invocation ->
                Mono.just(TypeLoanDomain.builder().id(1L).name("Personal").build())
        );

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .assertNext(pageDTO -> {
                    assert pageDTO.getPage() == page;
                    assert pageDTO.getSize() == size;
                    assert pageDTO.getTotalElements() == 28;
                    assert pageDTO.getTotalPages() == 5;
                    assert pageDTO.getContent().size() == expectedPage.size();

                    for (int i = 0; i < expectedPage.size(); i++) {
                        RequestLoanDomain actual = pageDTO.getContent().get(i);
                        RequestLoanDomain expected = expectedPage.get(i);

                        assert actual.getLoanTypeId().equals(expected.getLoanTypeId());
                        assert actual.getStatusId().equals(expected.getStatusId());
                        assert actual.getLoanTypeName().equals(expected.getLoanTypeName());
                    }
                })
                .verifyComplete();

    }

    @Test
    void shouldReturnEmptyPageWhenNoMatches() {
        List<Long> statuses = List.of(99L);
        int page = 1;
        int size = 6;

        when(gateway.findAll()).thenReturn(Flux.empty());
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .expectNextMatches(pageDTO ->
                        pageDTO.getPage() == page &&
                                pageDTO.getSize() == size &&
                                pageDTO.getTotalElements() == 0 &&
                                pageDTO.getContent().isEmpty()
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenPageOutOfBounds() {
        List<Long> statuses = List.of(1L);
        int page = 100;
        int size = 10;

        List<RequestLoanDomain> allFiltered = IntStream.range(0, 15)
                .mapToObj(i -> RequestLoanDomain.builder()
                        //.id(UUID.randomUUID().toString())
                        .statusId(1L)
                        .build())
                .toList();

        when(gateway.findAll()).thenReturn(Flux.fromIterable(allFiltered));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just((long) allFiltered.size()));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .expectNextMatches(pageDTO ->
                        pageDTO.getPage() == page &&
                                pageDTO.getSize() == size &&
                                pageDTO.getTotalElements() == 15 &&
                                pageDTO.getContent().isEmpty()
                )
                .verifyComplete();
    }
}

