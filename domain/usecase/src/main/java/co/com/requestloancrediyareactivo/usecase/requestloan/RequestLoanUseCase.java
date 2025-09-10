package co.com.requestloancrediyareactivo.usecase.requestloan;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.PendingRequestViewDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.SendObjectToQueue;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts.ICapacidadUseCase;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class RequestLoanUseCase implements IRequestLoanUseCase {
    private final RequestLoanRepositoryGateway gateway1;
    private final TypeLoanRepositoryGateway gateway2;
    private final ICapacidadUseCase capacidadUseCase;
    private static final String LOGP = "[crearr]";
   
   @Override
   public Mono<RequestLoanDomain> crearr(RequestLoanDomain domain, UserResponseDomain user) {
       final String trace = java.util.UUID.randomUUID().toString().substring(0, 8);

       if (!java.util.Objects.equals(domain.getDocument(), user.getIdNumber())) {
           return Mono.error(new RuntimeException("No autorizado: solo puedes crear solicitudes con tu propio documento"));
       }

       return gateway2.existsById(domain.getLoanTypeId())
               .flatMap(exists -> {
                   if (!exists) {
                       return Mono.error(new IllegalArgumentException("Tipo de prestamo no valido."));
                   }

                   var toSave = domain.toBuilder().statusId(1L).build();
                   return gateway1.save(toSave)
                           .flatMap(saved -> {

                               return gateway2.findTypeById(saved.getLoanTypeId())
                                       .flatMap(type -> {
                                           boolean auto = Boolean.TRUE.equals(type.getAuto_validation());

                                           java.math.BigInteger docNum = null;
                                           try { docNum = new java.math.BigInteger(saved.getDocument()); }
                                           catch (Exception e) {
                                               System.out.println(LOGP+"("+trace+") WARN cannot parse document '"+saved.getDocument()+"': "+e.getMessage());
                                           }

                                           return getPropsToQueue(saved.getId(), docNum)
                                                   .flatMap(props -> {
                                                       if (auto) {
                                                           return gateway1.sendToQueaueCapacidad(props)
                                                                   .doOnError(err -> System.out.println(LOGP+"("+trace+") queue ERROR "+err))
                                                                   .onErrorResume(err -> Mono.empty())
                                                                   .thenReturn(props);
                                                       } else {
                                                           return Mono.just(props);
                                                       }
                                                   })
                                                   .map(props -> {
                                                       return saved.toBuilder()
                                                               .loanTypeInterestrate(props.getLoanTypeInterestrate())
                                                               .auto_validation(props.getAuto_validation())
                                                               .loanTypeName(props.getLoanTypeName())
                                                               .baseSalary(props.getBaseSalary())
                                                               .totalMonthlyDebtApproved(props.getAvailableDebtCapacity())
                                                               .build();
                                                   });
                                       })
                                       .switchIfEmpty(Mono.fromSupplier(() -> {
                                           return saved;
                                       }));
                           });
               })
               .doOnError(err -> System.out.println(LOGP+"("+trace+") ERROR "+err.getClass().getSimpleName()+": "+err.getMessage()))
               .doFinally(sig -> System.out.println(LOGP+"("+trace+") end signal="+sig));
   }




    @Override
    public Mono<PageDTO<PendingRequestViewDTO>> findPendingForReview(List<Long> statuses, int page, int size) {
        int skip = (page - 1) * size;

        Mono<List<PendingRequestViewDTO>> contentMono =
                gateway1.findAll() // Flux<RequestLoanDomain>
                        .filter(entity -> statuses.contains(entity.getStatusId()))
                        .skip(skip)
                        .take(size)

                        .flatMap(domain ->
                                gateway2.findTypeById(domain.getLoanTypeId())
                                        // DEBUG: lo que viene de typeloan
                                        .doOnNext(type -> System.out.println(
                                                "[TYPE] id=" + type.getId()
                                                        + " name=" + type.getName()
                                                        + " interestRate=" + type.getLoanTypeInterestrate()
                                                        + " autoValidation=" + type.getAuto_validation()
                                        ))
                                        .map(type -> domain.toBuilder()
                                                .loanTypeName(type.getName())

                                                .loanTypeInterestrate(type.getLoanTypeInterestrate())
                                                .auto_validation(type.getAuto_validation())
                                                .build()
                                        )

                                        .doOnNext(enriched -> System.out.println(
                                                "[DOMAIN+] id=" + enriched.getId()
                                                        + " loanTypeName=" + enriched.getLoanTypeName()
                                                        + " loanTypeInterestRate=" + enriched.getLoanTypeInterestrate()
                                                        + " autoValidation=" + enriched.getAuto_validation()
                                        ))
                                        .defaultIfEmpty(domain)
                        )

                        .flatMap(domain ->
                                        gateway1.sumMonthlyDebtByIdNumber(domain.getDocument())
                                                .map(totalApprovedDebt -> {
                                                    PendingRequestViewDTO dto = PendingRequestViewDTO.builder()
                                                            .id(domain.getId())
                                                            .amount(domain.getAmount())
                                                            .term(domain.getTerm())
                                                            .document(domain.getDocument())
                                                            .name(domain.getName())
                                                            .email(domain.getEmail())
                                                            .statusId(domain.getStatusId())
                                                            .loanTypeId(domain.getLoanTypeId())
                                                            .loanTypeName(domain.getLoanTypeName())

                                                            .loanTypeInterestrate(domain.getLoanTypeInterestrate())
                                                            .auto_validation(domain.getAuto_validation())
                                                            .totalApprovedDebt(totalApprovedDebt)
                                                            .build();

                                                    return dto;
                                                })
                                , Math.max(2, size))
                        .collectList();

        Mono<Long> totalMono = gateway1.countByStatuses(statuses);

        return Mono.zip(contentMono, totalMono)
                .map(tuple -> {
                    List<PendingRequestViewDTO> content = tuple.getT1();
                    Long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / size);
                    return PageDTO.<PendingRequestViewDTO>builder()
                            .content(content)
                            .page(page)
                            .size(size)
                            .totalElements(total)
                            .totalPages(totalPages)
                            .build();
                });
    }


    //Entrante desde SQS QueauRebootMicroSolicitud
  @Override
public Mono<RequestLoanDomain> updateStatusAtomQuot(UUID id, int status, int atomVersion, double quotLoan) {

    final double effectiveQuot = (status == 4 || status==3) ? 0d : quotLoan; // <-- efectivamente final
    final BigDecimal fee = BigDecimal.valueOf(effectiveQuot).setScale(2, RoundingMode.HALF_UP);

    return gateway1.findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
        .flatMap(request -> {
            RequestLoanDomain actualizado = request.toBuilder()
                .statusId((long) status)
                .version((long) atomVersion)
                .calculatedMonthlyFee(fee) // queda 0 si status == 4
                .build();

            return gateway1.update(actualizado)
                .flatMap(saved -> getPropsToQueue(saved.getId(), safeBigInt(saved.getDocument()))
                    .flatMap(dto -> gateway1.sendToqueaueStatus(dto))
                    .thenReturn(saved));
        });
}


//ASESOR EVALUA Y DECIDE
    @Override
    public Mono<RequestLoanDomain> updateStatus(UUID id, int status, BigDecimal calculatedMonthlyFee) {

        return gateway1.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(request -> {

                    if (request.getStatusId() != 4L)
                        return Mono.error(new RuntimeException("Esta solicitud ya fue gestionada por el asesor"));
                    if (status == 1 || status == 4 || status == 5)
                        return Mono.error(new RuntimeException("Por favor proporcione un estado permitido 2 (Aprobado) o 3 (Rechazado)"));
                    if (status == 2 && (calculatedMonthlyFee == null || calculatedMonthlyFee.compareTo(BigDecimal.ZERO) <= 0))
                        return Mono.error(new RuntimeException("Para aprobar se requiere la cuota mensual (> 0)"));

                    final BigDecimal fee = calculatedMonthlyFee.setScale(2, java.math.RoundingMode.HALF_UP);

                    final RequestLoanDomain actualizado = request.toBuilder()
                            .statusId((long) status)
                            .calculatedMonthlyFee(fee)
                            .version(0L)
                            .build();

                    return gateway1.update(actualizado)
                            .flatMap(saved -> {
                                BigInteger docNum = safeBigInt(saved.getDocument());

                                return getPropsToQueue(saved.getId(), docNum)      // Mono<SendObjectToQueue>
                                        .flatMap(dto -> gateway1.sendToqueaueStatus(dto)   // ✔ ahora coincide la firma
                                                .thenReturn(saved))                            // devolvemos lo guardado
                                        .onErrorResume(err ->
                                                gateway1.update(request)                       // rollback si falla enriq/envío
                                                        .then(Mono.error(new RuntimeException(
                                                                "No se envió a SQS; se revirtió el estado.", err))));
                            });

                });
    }




    private static java.math.BigInteger safeBigInt(String s) {
        try { return s == null ? null : new java.math.BigInteger(s); }
        catch (Exception e) { return null; }
    }




    @Override
    public Mono<SendObjectToQueue> getPropsToQueue(UUID id, BigInteger documentNumber) {
        return gateway1.findByIdRelatedProps(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(req -> Mono.zip(
                        Mono.just(req),
                        gateway2.findTypeById(req.getLoanTypeId())
                                .switchIfEmpty(Mono.error(new RuntimeException("Tipo de préstamo no encontrado"))),
                        gateway1.sumMonthlyDebtByIdNumber(req.getDocument())
                                .defaultIfEmpty(BigDecimal.ZERO),       // <- Double
                        capacidadUseCase.getBaseSalary(req.getDocument())
                                .switchIfEmpty(Mono.just(0.0))
                                .onErrorReturn(0.0)        // <- Double
                ))
                .map(t -> {
                    var r      = t.getT1(); // RequestLoanDomain
                    var ty     = t.getT2(); // TypeLoanDomain
                    BigDecimal debt   = t.getT3(); // Double (ajustado)
                    Double salary = t.getT4(); // Double (ajustado)
                    BigDecimal fee = debt;
                    return SendObjectToQueue.builder()
                            .id(r.getId())
                            .amount(r.getAmount())
                            .term(r.getTerm())
                            .document(r.getDocument())
                            .name(r.getName())
                            .email(r.getEmail())
                            .statusId(r.getStatusId())
                            .loanTypeInterestrate(ty.getLoanTypeInterestrate())
                            .auto_validation(ty.getAuto_validation())
                            .loanTypeId(r.getLoanTypeId())
                            .loanTypeName(ty.getName())
                            .comment(r.getComment())
                            .availableDebtCapacity(fee)   // <- si tu builder espera BigDecimal, convierte aquí
                            .baseSalary(salary)            // <- idem
                            .build();
                });
    }


}
