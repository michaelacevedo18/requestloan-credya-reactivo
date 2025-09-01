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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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

    // -----------------------
    // apply()
    // -----------------------

    @Test
    void shouldRegisterLoanWithDefaultStatus() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0).term(12).email("cliente@correo.com")
                .loanTypeId(1L).document("123456").build();

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
                .amount(50000.0).term(12).email("cliente@correo.com")
                .loanTypeId(1L).document("000000").build();

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("No autorizado"))
                .verify();
    }

    @Test
    void shouldThrowErrorWhenLoanTypeDoesNotExist() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0).term(12).email("cliente@correo.com")
                .loanTypeId(99L).document("123456").build();

        when(gateway2.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Tipo de prestamo no valido"))
                .verify();
    }

    @Test
    void shouldHandleErrorWhenExistsFails() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0).term(12).email("cliente@correo.com")
                .loanTypeId(1L).document("123456").build();

        when(gateway2.existsById(1L)).thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("DB Error"))
                .verify();
    }

    @Test
    void shouldHandleErrorWhenSaveFails() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .amount(50000.0).term(12).email("cliente@correo.com")
                .loanTypeId(1L).document("123456").build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway.save(any())).thenReturn(Mono.error(new RuntimeException("Save failed")));

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Save failed"))
                .verify();
    }

    @Test
    void apply_shouldNotCallGatewaysOnDocumentMismatch() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .loanTypeId(1L).document("NO-MATCH").build();

        StepVerifier.create(useCase.apply(input, user))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("No autorizado"))
                .verify();

        verifyNoInteractions(gateway);
        verifyNoInteractions(gateway2);
    }

    @Test
    void apply_shouldSaveWithStatusIdOne() {
        RequestLoanDomain input = RequestLoanDomain.builder()
                .loanTypeId(5L).document(user.getIdNumber()).build();

        when(gateway2.existsById(5L)).thenReturn(Mono.just(true));
        when(gateway.save(any(RequestLoanDomain.class)))
                .thenAnswer(inv -> Mono.just((RequestLoanDomain) inv.getArgument(0)));

        StepVerifier.create(useCase.apply(input, user))
                .expectNextMatches(r -> r.getStatusId() != null && r.getStatusId().equals(1L))
                .verifyComplete();

        verify(gateway2).existsById(5L);
        verify(gateway).save(argThat(saved -> saved.getStatusId().equals(1L)));
    }

    // -----------------------
    // findPendingForReview()
    // -----------------------

    @Test
    void shouldReturnPagedResults() {
        List<Long> statuses = List.of(1L, 2L, 3L);
        int page = 1, size = 6, skip = (page - 1) * size;

        List<RequestLoanDomain> allFiltered = IntStream.range(0, 28)
                .mapToObj(i -> RequestLoanDomain.builder()
                        .statusId(1L).loanTypeId(1L).build())
                .toList();

        List<RequestLoanDomain> expectedPage = allFiltered.stream()
                .skip(skip).limit(size)
                .map(loan -> loan.toBuilder().loanTypeName("Personal").build())
                .toList();

        when(gateway.findAll()).thenReturn(Flux.fromIterable(allFiltered));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just((long) allFiltered.size()));
        when(gateway2.findTypeById(1L)).thenAnswer(inv ->
                Mono.just(TypeLoanDomain.builder().id(1L).name("Personal").build()));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .assertNext(pageDTO -> {
                    assert pageDTO.getPage() == page;
                    assert pageDTO.getSize() == size;
                    assert pageDTO.getTotalElements() == 28;
                    assert pageDTO.getTotalPages() == 5;
                    assert pageDTO.getContent().size() == expectedPage.size();
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatches() {
        List<Long> statuses = List.of(99L);
        int page = 1, size = 6;

        when(gateway.findAll()).thenReturn(Flux.empty());
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .expectNextMatches(pageDTO ->
                        pageDTO.getPage() == page &&
                                pageDTO.getSize() == size &&
                                pageDTO.getTotalElements() == 0 &&
                                pageDTO.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenPageOutOfBounds() {
        List<Long> statuses = List.of(1L);
        int page = 100, size = 10;

        List<RequestLoanDomain> allFiltered = IntStream.range(0, 15)
                .mapToObj(i -> RequestLoanDomain.builder()
                        .statusId(1L).build())
                .toList();

        when(gateway.findAll()).thenReturn(Flux.fromIterable(allFiltered));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just((long) allFiltered.size()));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .expectNextMatches(pageDTO ->
                        pageDTO.getPage() == page &&
                                pageDTO.getSize() == size &&
                                pageDTO.getTotalElements() == 15 &&
                                pageDTO.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void findPendingForReview_shouldComputeTotalPagesExactDivision() {
        List<Long> statuses = List.of(1L);
        int page = 2, size = 5;

        List<RequestLoanDomain> allFiltered = IntStream.range(0, 10)
                .mapToObj(i -> RequestLoanDomain.builder()
                        .statusId(1L).loanTypeId(1L).build())
                .toList();

        when(gateway.findAll()).thenReturn(Flux.fromIterable(allFiltered));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just(10L));
        when(gateway2.findTypeById(1L))
                .thenReturn(Mono.just(TypeLoanDomain.builder().id(1L).name("Personal").build()));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .assertNext(p -> {
                    assert p.getTotalElements() == 10;
                    assert p.getTotalPages() == 2;
                    assert p.getPage() == 2;
                    assert p.getSize() == 5;
                    assert p.getContent().size() == 5;
                })
                .verifyComplete();
    }

    @Test
    void findPendingForReview_shouldPropagateErrorWhenTypeLookupFails() {
        // Cubre la rama donde el flatMap hacia findTypeById falla
        List<Long> statuses = List.of(1L);
        int page = 1, size = 5;

        List<RequestLoanDomain> items = List.of(
                RequestLoanDomain.builder().statusId(1L).loanTypeId(1L).build()
        );

        when(gateway.findAll()).thenReturn(Flux.fromIterable(items));
        when(gateway.countByStatuses(statuses)).thenReturn(Mono.just(1L));
        when(gateway2.findTypeById(1L))
                .thenReturn(Mono.error(new RuntimeException("type lookup failed")));

        StepVerifier.create(useCase.findPendingForReview(statuses, page, size))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("type lookup failed"))
                .verify();
    }

    // -----------------------
    // updateStatus()
    // -----------------------

    @Test
    void updateStatus_shouldErrorWhenRequestNotFound() {
        UUID id = UUID.randomUUID();
        when(gateway.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStatus(id, 2, "ok"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("Solicitud no encontrada"))
                .verify();

        verify(gateway, times(1)).findById(id);
        verify(gateway, never()).update(any());
        verify(gateway, never()).sendStatusUpdateMessage(any());
    }

    @Test
    void updateStatus_shouldErrorWhenAlreadyManaged() {
        UUID id = UUID.randomUUID();
        RequestLoanDomain existing = RequestLoanDomain.builder()
                .id(id)
                .statusId(2L)
                .build();

        when(gateway.findById(id)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.updateStatus(id, 3, "coment"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("ya fue gestionada"))
                .verify();

        verify(gateway, times(1)).findById(id);
        verify(gateway, never()).update(any());
        verify(gateway, never()).sendStatusUpdateMessage(any());
    }

    @Test
    void updateStatus_shouldErrorWhenStatusIsPendingAgain() {
        UUID id = UUID.randomUUID();
        RequestLoanDomain existing = RequestLoanDomain.builder()
                .id(id)
                .statusId(1L)
                .build();

        when(gateway.findById(id)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.updateStatus(id, 1, ""))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("estado diferente a Pendiente"))
                .verify();

        verify(gateway, times(1)).findById(id);
        verify(gateway, never()).update(any());
        verify(gateway, never()).sendStatusUpdateMessage(any());
    }

    @Test
    void updateStatus_shouldUpdateAndSendMessage_successPath() {
        UUID id = UUID.randomUUID();
        RequestLoanDomain existing = RequestLoanDomain.builder()
                .id(id)
                .statusId(1L)
                .comment(null)
                .build();

        RequestLoanDomain afterUpdate = existing.toBuilder()
                .statusId(2L)
                .comment("aprobado")
                .build();

        when(gateway.findById(id)).thenReturn(Mono.just(existing));
        when(gateway.update(any(RequestLoanDomain.class))).thenReturn(Mono.just(afterUpdate));
        when(gateway.sendStatusUpdateMessage(afterUpdate)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStatus(id, 2, "aprobado"))
                .expectNext(afterUpdate)
                .verifyComplete();

        verify(gateway, times(1)).update(any(RequestLoanDomain.class));
        verify(gateway, times(1)).sendStatusUpdateMessage(afterUpdate);
    }

    @Test
    void updateStatus_shouldRollbackWhenSqsFails() {
        UUID id = UUID.randomUUID();
        RequestLoanDomain existing = RequestLoanDomain.builder()
                .id(id)
                .statusId(1L)
                .build();

        RequestLoanDomain afterUpdate = existing.toBuilder()
                .statusId(3L)
                .comment("rechazado")
                .build();

        when(gateway.findById(id)).thenReturn(Mono.just(existing));
        when(gateway.update(any(RequestLoanDomain.class))).thenReturn(Mono.just(afterUpdate));
        when(gateway.sendStatusUpdateMessage(afterUpdate))
                .thenReturn(Mono.error(new RuntimeException("SQS down")));
        when(gateway.update(existing)).thenReturn(Mono.just(existing)); // rollback

        StepVerifier.create(useCase.updateStatus(id, 3, "rechazado"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("No se envio el mensaje a SQS"))
                .verify();

        verify(gateway, times(1)).sendStatusUpdateMessage(afterUpdate);
        verify(gateway, times(2)).update(any()); // 1 update + 1 rollback
    }

    @Test
    void updateStatus_shouldPropagateErrorWhenFirstUpdateFails() {
        // Cubre la rama donde falla gateway.update(actualizado) ANTES del envío a SQS
        UUID id = UUID.randomUUID();
        RequestLoanDomain existing = RequestLoanDomain.builder()
                .id(id)
                .statusId(1L)
                .build();

        when(gateway.findById(id)).thenReturn(Mono.just(existing));
        when(gateway.update(any(RequestLoanDomain.class)))
                .thenReturn(Mono.error(new RuntimeException("update failed")));

        StepVerifier.create(useCase.updateStatus(id, 2, "aprobado"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("update failed"))
                .verify();

        verify(gateway, times(1)).update(any(RequestLoanDomain.class));
        verify(gateway, never()).sendStatusUpdateMessage(any());
    }
}

