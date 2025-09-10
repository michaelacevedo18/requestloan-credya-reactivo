/**
package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts.ICapacidadUseCase;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestLoanUseCaseTest {

    @org.mockito.Mock RequestLoanRepositoryGateway gateway1;
    @org.mockito.Mock TypeLoanRepositoryGateway gateway2;
    @org.mockito.Mock ICapacidadUseCase capacidadUseCase;

    private RequestLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestLoanUseCase(gateway1, gateway2, capacidadUseCase);
    }

    private RequestLoanDomain baseDomain(UUID id) {
        return RequestLoanDomain.builder()
                .id(id)
                .amount(5000000D) // BigDecimal como en tu dominio
                .term(24)
                .document("1049632878")
                .name("Michael")
                .email("michael@example.com")
                .statusId(1L)
                .loanTypeId(1L)
                .build();
    }

    private TypeLoanDomain baseTypeLoan() {
        return TypeLoanDomain.builder()
                .id(1L)
                .name("Libre Inversión")
                .loanTypeInterestrate(12.0)
                .auto_validation(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPropsToQueue_devuelveDTO_conDeudaYSalario_yTipo() {
        UUID id = UUID.randomUUID();

        // Puedes seguir usando el domain helper si te sirve para poblar campos
        var req = baseDomain(id); // amount BigDecimal, term, document, etc.

        // 🔧 IMPORTANTE: findByIdRelatedProps debe devolver SendObjectToQueue
        var preDto = SendObjectToQueue.builder()
                .id(req.getId())
                .amount(req.getAmount())             // BigDecimal
                .term(req.getTerm())                 // Integer
                .document(req.getDocument())
                .name(req.getName())
                .email(req.getEmail())
                .statusId(req.getStatusId())
                .loanTypeId(req.getLoanTypeId())
                .comment(req.getComment())
                // estos dos normalmente los calcula el use case; acá da igual si vienen nulos
                .availableDebtCapacity(null)
                .baseSalary(null)
                .build();

        // ✅ Stub correcto: Mono<SendObjectToQueue>
        org.mockito.Mockito.when(gateway1.findByIdRelatedProps(id))
                .thenReturn(Mono.just(preDto));

        // Lo demás como antes
        org.mockito.Mockito.when(gateway2.findTypeById(1L))
                .thenReturn(Mono.just(baseTypeLoan())); // name + interest + auto_validation

        org.mockito.Mockito.when(gateway1.sumMonthlyDebtByIdNumber("1049632878"))
                .thenReturn(Mono.just(new BigDecimal("4707025.30"))); // deuda total aprobada

        org.mockito.Mockito.when(capacidadUseCase.getBaseSalary("1049632878"))
                .thenReturn(Mono.just(6_500_000.0)); // salario

        StepVerifier.create(useCase.getPropsToQueue(id, new BigInteger("1049632878")))
                .assertNext(dto -> {
                    System.out.println("[DTO OUT] " + dto);
                    assertEquals(id, dto.getId());
                    assertEquals("Libre Inversión", dto.getLoanTypeName());
                    assertEquals(12.0, dto.getLoanTypeInterestrate());
                    assertEquals(0, dto.getAvailableDebtCapacity().compareTo(new BigDecimal("4707025.30")));
                    assertEquals(6_500_000.0, dto.getBaseSalary());
                })
                .verifyComplete();
    }

}**/
package co.com.requestloancrediyareactivo.usecase.requestloan.requestloan.requestloan;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts.ICapacidadUseCase;
import co.com.requestloancrediyareactivo.usecase.requestloan.RequestLoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestLoanUseCaseTest {

    @org.mockito.Mock RequestLoanRepositoryGateway gateway1;
    @org.mockito.Mock TypeLoanRepositoryGateway gateway2;
    @org.mockito.Mock ICapacidadUseCase capacidadUseCase;

    private RequestLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestLoanUseCase(gateway1, gateway2, capacidadUseCase);
    }

    // ------------ Helpers ------------
    private RequestLoanDomain baseDomain(UUID id) {
        return RequestLoanDomain.builder()
                .id(id)
                .amount(5_000_000D)   // Double (como en tu modelo actual)
                .term(24)
                .document("1049632878")
                .name("Michael")
                .email("michael@example.com")
                .statusId(1L)
                .loanTypeId(1L)
                .build();
    }

    private TypeLoanDomain baseTypeLoan(boolean autoValidation) {
        return TypeLoanDomain.builder()
                .id(1L)
                .name("Libre Inversión")
                .loanTypeInterestrate(12.0)
                .auto_validation(autoValidation)
                .build();
    }

    private UserResponseDomain baseUser() {
        return UserResponseDomain.builder().idNumber("1049632878").build();
    }

    private SendObjectToQueue preDtoFrom(RequestLoanDomain d) {
        return SendObjectToQueue.builder()
                .id(d.getId())
                .amount(d.getAmount())
                .term(d.getTerm())
                .document(d.getDocument())
                .name(d.getName())
                .email(d.getEmail())
                .statusId(d.getStatusId())
                .loanTypeId(d.getLoanTypeId())
                .comment(d.getComment())
                .build();
    }

    // ========== 1) crearr: auto_validation = true ==========
    @Test
    void crearr_autoTrue_enviaACola_y_enriquece() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id);
        var saved = input.toBuilder().statusId(1L).build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway1.save(any())).thenAnswer(inv -> Mono.just((RequestLoanDomain) inv.getArgument(0)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));

        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(saved)));
        when(gateway1.sumMonthlyDebtByIdNumber("1049632878")).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary("1049632878")).thenReturn(Mono.just(6_500_000.0));

        when(gateway1.sendToQueaueCapacidad(any(SendObjectToQueue.class))).thenReturn(Mono.<Void>empty());

        StepVerifier.create(useCase.crearr(input, baseUser()))
                .assertNext(out -> {
                    assertEquals(1L, out.getStatusId());
                    assertEquals("Libre Inversión", out.getLoanTypeName());
                    assertEquals(12.0, out.getLoanTypeInterestrate());
                    assertTrue(out.getAuto_validation());
                    assertEquals(6_500_000.0, out.getBaseSalary());
                    assertEquals(0, out.getTotalMonthlyDebtApproved().compareTo(BigDecimal.ZERO));
                })
                .verifyComplete();

        verify(gateway1, times(1)).sendToQueaueCapacidad(any(SendObjectToQueue.class));
    }

    // ========== 2) crearr: auto_validation = false (no envía cola) ==========
    @Test
    void crearr_autoFalse_noEnviaACola_pero_enriquece() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id);
        var saved = input.toBuilder().statusId(1L).build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway1.save(any())).thenAnswer(inv -> Mono.just((RequestLoanDomain) inv.getArgument(0)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(false)));

        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(saved)));
        when(gateway1.sumMonthlyDebtByIdNumber("1049632878")).thenReturn(Mono.just(new BigDecimal("123")));
        when(capacidadUseCase.getBaseSalary("1049632878")).thenReturn(Mono.just(1_000_000.0));

        StepVerifier.create(useCase.crearr(input, baseUser()))
                .assertNext(out -> {
                    assertEquals("Libre Inversión", out.getLoanTypeName());
                    assertEquals(12.0, out.getLoanTypeInterestrate());
                    assertEquals(1_000_000.0, out.getBaseSalary());
                    assertEquals(0, out.getTotalMonthlyDebtApproved().compareTo(new BigDecimal("123")));
                })
                .verifyComplete();

        verify(gateway1, never()).sendToQueaueCapacidad(any());
    }

    // ========== 3) crearr: documento ≠ usuario ==========
    @Test
    void crearr_documentoNoCoincide_lanzaError() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id).toBuilder().document("999").build();
        var user  = UserResponseDomain.builder().idNumber("1049632878").build();

        StepVerifier.create(useCase.crearr(input, user))
                .expectErrorMatches(ex -> ex.getMessage().contains("No autorizado"))
                .verify();
    }

    // ========== 4) crearr: tipo no existe ==========
    @Test
    void crearr_tipoNoValido_lanzaError() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id);

        when(gateway2.existsById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.crearr(input, baseUser()))
                .expectErrorMatches(ex -> ex.getMessage().contains("Tipo de prestamo no valido"))
                .verify();
    }

    // ========== 5) findPendingForReview: enriquecido + paginación ==========
    @Test
    void findPendingForReview_enriquece_yPagina() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        var d1 = baseDomain(id1).toBuilder().statusId(4L).build();
        var d2 = baseDomain(id2).toBuilder().statusId(4L).build();

        when(gateway1.findAll()).thenReturn(Flux.just(d1, d2));
        when(gateway2.findTypeById(anyLong())).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(new BigDecimal("555")));
        when(gateway1.countByStatuses(List.of(4L))).thenReturn(Mono.just(2L));

        StepVerifier.create(useCase.findPendingForReview(List.of(4L), 1, 1)) // page 1, size 1
                .assertNext(page -> {
                    assertEquals(2L, page.getTotalElements());
                    assertEquals(2, page.getTotalPages());
                    assertEquals(1, page.getContent().size());
                    var dto = page.getContent().get(0);
                    assertEquals("Libre Inversión", dto.getLoanTypeName());
                    assertEquals(0, dto.getTotalApprovedDebt().compareTo(new BigDecimal("555")));
                })
                .verifyComplete();
    }

    // ========== 6) findPendingForReview: defaultIfEmpty (tipo no encontrado) ==========
    @Test
    void findPendingForReview_tipoNoEncontrado_noEnriquece() {
        UUID id = UUID.randomUUID();
        var d = baseDomain(id).toBuilder().statusId(4L).build();

        when(gateway1.findAll()).thenReturn(Flux.just(d));
        when(gateway2.findTypeById(anyLong())).thenReturn(Mono.empty()); // <- fuerza defaultIfEmpty
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(gateway1.countByStatuses(List.of(4L))).thenReturn(Mono.just(1L));

        StepVerifier.create(useCase.findPendingForReview(List.of(4L), 1, 10))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertNull(page.getContent().get(0).getLoanTypeName()); // no se enriqueció
                })
                .verifyComplete();
    }

    // ========== 7) updateStatusAtomQuot: manual (fee 0) ==========
    @Test
    void updateStatusAtomQuot_manual_feeCero_yEncola() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(1L).build();

        when(gateway1.findById(id)).thenReturn(Mono.just(existing));
        when(gateway1.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(existing)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary(anyString())).thenReturn(Mono.just(0.0));
        when(gateway1.sendToqueaueStatus(any(SendObjectToQueue.class))).thenReturn(Mono.<Void>empty());

        StepVerifier.create(useCase.updateStatusAtomQuot(id, 4, 9, 12345.67))
                .assertNext(out -> {
                    assertEquals(4L, out.getStatusId());
                    assertEquals(9L, out.getVersion());
                    assertEquals(0, out.getCalculatedMonthlyFee().compareTo(new BigDecimal("0.00")));
                })
                .verifyComplete();
    }

    // ========== 8) updateStatusAtomQuot: aprobado (fee > 0) ==========
    @Test
    void updateStatusAtomQuot_aprobado_feeSeteado_yEncola() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(1L).build();

        when(gateway1.findById(id)).thenReturn(Mono.just(existing));
        when(gateway1.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(existing)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary(anyString())).thenReturn(Mono.just(0.0));
        when(gateway1.sendToqueaueStatus(any(SendObjectToQueue.class))).thenReturn(Mono.<Void>empty());

        StepVerifier.create(useCase.updateStatusAtomQuot(id, 2, 1, 999.99))
                .assertNext(out -> assertEquals(0, out.getCalculatedMonthlyFee().compareTo(new BigDecimal("999.99"))))
                .verifyComplete();
    }

    // ========== 9) updateStatus: validaciones ==========
    @Test
    void updateStatus_rechaza_siEstadoPrevioNoEs4() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(2L).build();
        when(gateway1.findById(id)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.updateStatus(id, 2, new BigDecimal("1")))
                .expectErrorMatches(ex -> ex.getMessage().contains("ya fue gestionada"))
                .verify();
    }

    @Test
    void updateStatus_rechaza_siEstadoObjetivoInvalido() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(4L).build();
        when(gateway1.findById(id)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.updateStatus(id, 5, new BigDecimal("1")))
                .expectErrorMatches(ex -> ex.getMessage().contains("estado permitido"))
                .verify();
    }

    @Test
    void updateStatus_rechaza_aprobarSinCuota() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(4L).build();
        when(gateway1.findById(id)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.updateStatus(id, 2, new BigDecimal("0")))
                .expectErrorMatches(ex -> ex.getMessage().contains("Para aprobar se requiere la cuota"))
                .verify();
    }

    // ========== 10) updateStatus: rollback cuando falla envío ==========
    @Test
    void updateStatus_fallaEnSqs_haceRollback_yPropagaError() {
        UUID id = UUID.randomUUID();
        var original = baseDomain(id).toBuilder().statusId(4L).build();

        when(gateway1.findById(id)).thenReturn(Mono.just(original));
        when(gateway1.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(original)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary(anyString())).thenReturn(Mono.just(0.0));

        when(gateway1.sendToqueaueStatus(any(SendObjectToQueue.class)))
                .thenReturn(Mono.error(new RuntimeException("SQS caído")));

        StepVerifier.create(useCase.updateStatus(id, 2, new BigDecimal("10")))
                .expectErrorMatches(ex -> ex.getMessage().contains("No se envió a SQS; se revirtió el estado"))
                .verify();

        ArgumentCaptor<RequestLoanDomain> captor = ArgumentCaptor.forClass(RequestLoanDomain.class);
        verify(gateway1, times(2)).update(captor.capture());

        // segunda llamada restaura el original
        var last = captor.getAllValues().get(1);
        assertEquals(original.getStatusId(), last.getStatusId());
    }

    // ========== 11) getPropsToQueue: éxito (deuda empty y salario con error) ==========
    @Test
    void getPropsToQueue_deudaEmpty_ySalarioErrorDevuelveCero() {
        UUID id = UUID.randomUUID();
        var req = baseDomain(id);

        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(req)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber("1049632878")).thenReturn(Mono.empty()); // defaultIfEmpty -> 0
        when(capacidadUseCase.getBaseSalary("1049632878"))      // onErrorReturn -> 0.0
                .thenReturn(Mono.error(new RuntimeException("service down")));

        StepVerifier.create(useCase.getPropsToQueue(id, new BigInteger("1049632878")))
                .assertNext(dto -> {
                    assertEquals(0, dto.getAvailableDebtCapacity().compareTo(BigDecimal.ZERO));
                    assertEquals(0.0, dto.getBaseSalary());
                })
                .verifyComplete();
    }

    // ========== 12) getPropsToQueue: solicitud no encontrada ==========
    @Test
    void getPropsToQueue_noExisteSolicitud_error() {
        UUID id = UUID.randomUUID();
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getPropsToQueue(id, null))
                .expectErrorMatches(ex -> ex.getMessage().contains("Solicitud no encontrada"))
                .verify();
    }

    // ========== 13) getPropsToQueue: tipo no encontrado ==========
    @Test
    void getPropsToQueue_tipoNoEncontrado_error() {
        UUID id = UUID.randomUUID();
        var req = baseDomain(id);

        // solicitud existe
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(req)));

        // tipo NO existe -> debe fallar con ese mensaje
        when(gateway2.findTypeById(1L)).thenReturn(Mono.empty());

        // 👇 IMPORTANTE: provee Monos válidos para que Mono.zip no reciba null
        when(gateway1.sumMonthlyDebtByIdNumber(anyString()))
                .thenReturn(Mono.just(BigDecimal.ZERO));   // o Mono.empty()
        when(capacidadUseCase.getBaseSalary(anyString()))
                .thenReturn(Mono.just(0.0));               // o Mono.error(...) si quieres

        StepVerifier.create(useCase.getPropsToQueue(id, new BigInteger("1049632878")))
                .expectErrorMatches(ex -> ex.getMessage().contains("Tipo de préstamo no encontrado"))
                .verify();
    }
    // ========== A) crearr: tipo no encontrado -> switchIfEmpty ==========
    @Test
    void crearr_tipoLookupVacio_retornaSaved_sinEncolarNiEnriquecer() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id);
        var saved = input.toBuilder().statusId(1L).build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway1.save(any())).thenAnswer(inv -> Mono.just((RequestLoanDomain) inv.getArgument(0)));

        // 👇 fuerza el branch .switchIfEmpty(...) después de save()
        when(gateway2.findTypeById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.crearr(input, baseUser()))
                .assertNext(out -> {
                    // Es el 'saved' sin enriquecimiento:
                    assertEquals(1L, out.getStatusId());
                    assertNull(out.getLoanTypeName());
                    assertNull(out.getLoanTypeInterestrate());
                    assertNull(out.getAuto_validation());
                    assertNull(out.getBaseSalary());
                    assertNull(out.getTotalMonthlyDebtApproved());
                })
                .verifyComplete();

        // No hubo envío a cola porque no hubo tipo
        verify(gateway1, never()).sendToQueaueCapacidad(any());
    }

    // ========== B) crearr: auto=true pero falla envío a cola -> onErrorResume ==========
    @Test
    void crearr_autoTrue_envioColaFalla_noPropagaError_yEnriqueceIgual() {
        UUID id = UUID.randomUUID();
        var input = baseDomain(id);
        var saved = input.toBuilder().statusId(1L).build();

        when(gateway2.existsById(1L)).thenReturn(Mono.just(true));
        when(gateway1.save(any())).thenAnswer(inv -> Mono.just((RequestLoanDomain) inv.getArgument(0)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));

        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(saved)));
        when(gateway1.sumMonthlyDebtByIdNumber("1049632878")).thenReturn(Mono.just(new BigDecimal("7")));
        when(capacidadUseCase.getBaseSalary("1049632878")).thenReturn(Mono.just(2_000_000.0));

        // 👇 cubre .onErrorResume(err -> Mono.empty()).thenReturn(props)
        when(gateway1.sendToQueaueCapacidad(any(SendObjectToQueue.class)))
                .thenReturn(Mono.error(new RuntimeException("SQS down")));

        StepVerifier.create(useCase.crearr(input, baseUser()))
                .assertNext(out -> {
                    assertEquals("Libre Inversión", out.getLoanTypeName());
                    assertEquals(12.0, out.getLoanTypeInterestrate());
                    assertTrue(out.getAuto_validation());
                    assertEquals(2_000_000.0, out.getBaseSalary());
                    assertEquals(0, out.getTotalMonthlyDebtApproved().compareTo(new BigDecimal("7")));
                })
                .verifyComplete();

        verify(gateway1, times(1)).sendToQueaueCapacidad(any());
    }

    // ========== C) updateStatusAtomQuot: status=3 (RECHAZADO) -> fee 0 ==========
    @Test
    void updateStatusAtomQuot_rechazado_feeCero_yEncola() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(1L).build();

        when(gateway1.findById(id)).thenReturn(Mono.just(existing));
        when(gateway1.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(existing)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary(anyString())).thenReturn(Mono.just(0.0));
        when(gateway1.sendToqueaueStatus(any(SendObjectToQueue.class))).thenReturn(Mono.<Void>empty());

        StepVerifier.create(useCase.updateStatusAtomQuot(id, 3, 5, 12345.67))
                .assertNext(out -> {
                    assertEquals(3L, out.getStatusId());
                    assertEquals(5L, out.getVersion());
                    assertEquals(0, out.getCalculatedMonthlyFee().compareTo(new BigDecimal("0.00")));
                })
                .verifyComplete();
    }

    // ========== D) updateStatus: de 4 -> 3 (rechazo) ==========
    @Test
    void updateStatus_de4a3_rechazado_ok_yEncola() {
        UUID id = UUID.randomUUID();
        var existing = baseDomain(id).toBuilder().statusId(4L).build();

        when(gateway1.findById(id)).thenReturn(Mono.just(existing));
        when(gateway1.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(gateway1.findByIdRelatedProps(id)).thenReturn(Mono.just(preDtoFrom(existing)));
        when(gateway2.findTypeById(1L)).thenReturn(Mono.just(baseTypeLoan(true)));
        when(gateway1.sumMonthlyDebtByIdNumber(anyString())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(capacidadUseCase.getBaseSalary(anyString())).thenReturn(Mono.just(0.0));
        when(gateway1.sendToqueaueStatus(any(SendObjectToQueue.class))).thenReturn(Mono.<Void>empty());

        StepVerifier.create(useCase.updateStatus(id, 3, BigDecimal.ZERO))
                .assertNext(out -> {
                    assertEquals(3L, out.getStatusId());
                    assertEquals(0, out.getCalculatedMonthlyFee().compareTo(BigDecimal.ZERO));
                    assertEquals(0L, out.getVersion());
                })
                .verifyComplete();
    }

}
