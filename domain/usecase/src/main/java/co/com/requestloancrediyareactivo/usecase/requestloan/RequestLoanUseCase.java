package co.com.requestloancrediyareactivo.usecase.requestloan;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.CapacidadGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import co.com.requestloancrediyareactivo.usecase.capacity.primaryPorts.ICapacidadUseCase;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RequestLoanUseCase implements IRequestLoanUseCase {
    private final RequestLoanRepositoryGateway gateway1;
    private final TypeLoanRepositoryGateway gateway2;
    private final ICapacidadUseCase capacidadUseCase;

   /** @Override
    public Mono<RequestLoanDomain> apply(RequestLoanDomain domain, UserResponseDomain user) {
        if (!domain.getDocument().equals(user.getIdNumber())) {
            return Mono.error(new RuntimeException("No autorizado: solo puedes crear solicitudes con tu propio documento"));
        }
        return gateway2.existsById(domain.getLoanTypeId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Tipo de prestamo no valido"));
                    }
                    RequestLoanDomain enriched = domain.toBuilder()
                            .statusId(1L)
                            .build();
                    return gateway1.save(enriched);
                });
    }
**/
   @Override
   public Mono<RequestLoanDomain> apply(RequestLoanDomain domain, UserResponseDomain user) {
       if (!domain.getDocument().equals(user.getIdNumber())) {
           return Mono.error(new RuntimeException("No autorizado: solo puedes crear solicitudes con tu propio documento"));
       }

       return gateway2.existsById(domain.getLoanTypeId())
               .flatMap(exists -> {
                   if (!exists) return Mono.error(new IllegalArgumentException("Tipo de prestamo no valido ."));

                   var toSave = domain.toBuilder()
                           .statusId(1L) // Pendiente de revisión
                           .build();

                   return gateway1.save(toSave)
                           .flatMap(saved ->
                                   gateway2.findTypeById(saved.getLoanTypeId())
                                           .flatMap(type -> {
                                               boolean auto = Boolean.TRUE.equals(type.getAuto_validation());

                                               // documento -> BigInteger seguro
                                               BigInteger docNum;
                                               try { docNum = new BigInteger(saved.getDocument()); }
                                               catch (Exception e) { docNum = null; }

                                               // payload enriquecido (props para la lambda)
                                               return getPropsToQueue(saved.getId(), docNum)
                                                       .doOnNext(p -> {
                                                           if (auto) {
                                                               System.out.println("Se debe ejecutar la lambda");

                                                           }
                                                       })
                                                       // <-- Este es el truco: devolver el DOMAIN enriquecido para la respuesta
                                                       .map(p -> saved.toBuilder()
                                                               .loanTypeInterestrate(p.getLoanTypeInterestrate())
                                                               .auto_validation(p.getAuto_validation())
                                                               .loanTypeName(p.getLoanTypeName())
                                                               .baseSalary(p.getBaseSalary())
                                                               .totalMonthlyDebtApproved(p.getAvailableDebtCapacity())
                                                               .build()
                                                       );
                                           })
                                           // si no hay tipo, devuelve lo guardado tal cual
                                           .switchIfEmpty(Mono.just(saved))
                           );
               });
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


    @Override
    public Mono<RequestLoanDomain> updateStatus(UUID id, int status, String comentario) {
        return gateway1.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(request -> {
                    if (request.getStatusId() != 1L) {
                        return Mono.error(new RuntimeException("Esta solicitud ya fue gestionada por el asesor"));
                    }
                    if (status == 1) {
                        return Mono.error(new RuntimeException("Por favor proporcione un estado diferente a Pendiente de revisión"));
                    }

                    RequestLoanDomain actualizado = request.toBuilder()
                            .statusId((long) status)
                            .comment(comentario)
                            .build();

                    System.out.println("Solicitud actualizada: " + actualizado);

                    return gateway1.update(actualizado)
                            .flatMap(saved ->
                                    gateway1.sendToqueaueStatus(saved)
                                            .thenReturn(saved)
                                            .onErrorResume(error -> {
                                                System.out.println("Error al enviar a SQS, se revertirá la solicitud");
                                                return gateway1.update(request) // rollback
                                                        .then(Mono.error(new RuntimeException("No se envio el mensaje a SQS el estado sigue siendo Pendiente", error)));
                                            })
                            );
                });
    }

    @Override
    public Mono<SendObjectToQueue> getPropsToQueue(UUID id, BigInteger documentNumber) {
        return gateway1.findByIdRelatedProps(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .flatMap(req ->

                        Mono.zip(
                                Mono.just(req),
                                gateway2.findTypeById(req.getLoanTypeId())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Tipo de préstamo no encontrado"))),
                                gateway1.sumMonthlyDebtByIdNumber(req.getDocument())
                                        .defaultIfEmpty(Double.valueOf(0.0D)),

                                capacidadUseCase.getBaseSalary(req.getDocument())
                                        .switchIfEmpty(Mono.just(0D))
                                        .onErrorReturn(0D) // fallback
                        )
                )
                .map(t -> {
                    var r      = t.getT1(); // RequestLoanDomain
                    var ty     = t.getT2(); // TypeLoanDomain
                    var debt   = t.getT3(); // BigDecimal
                    var salary = t.getT4(); // BigDecimal
                    System.out.println("Salary--: "+ salary );
                    System.out.println("Debt--: "+ debt );
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
                            .availableDebtCapacity(debt)
                            .baseSalary(salary)                 // <-- YA NO ES 55B
                            .build();
                });
    }


}
