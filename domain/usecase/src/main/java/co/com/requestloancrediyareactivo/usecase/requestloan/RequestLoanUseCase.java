package co.com.requestloancrediyareactivo.usecase.requestloan;

import java.util.List;
import java.util.UUID;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.RequestLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.gateways.TypeLoanRepositoryGateway;
import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.PendingRequestViewDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts.IRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RequestLoanUseCase implements IRequestLoanUseCase {
    private final RequestLoanRepositoryGateway gateway1;
    private final TypeLoanRepositoryGateway gateway2;


    @Override
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

    @Override
    public Mono<PageDTO<PendingRequestViewDTO>> findPendingForReview(List<Long> statuses, int page, int size) {
        int skip = (page - 1) * size;

        // 1) Contenido paginado + nombre del tipo de préstamo
        Mono<List<PendingRequestViewDTO>> contentMono =
                gateway1.findAll()                                  // Flux<RequestLoanDomain>
                        .filter(entity -> statuses.contains(entity.getStatusId()))
                        .skip(skip)
                        .take(size)

                        .flatMap(domain ->
                                gateway2.findTypeById(domain.getLoanTypeId())
                                        .map(type -> domain.toBuilder().loanTypeName(type.getName()).build())
                        )

                        .flatMap(domain ->
                                        gateway1.sumMonthlyDebtByIdNumber(domain.getDocument())
                                                .map(totalApprovedDebt ->
                                                        PendingRequestViewDTO.builder()
                                                                .id(domain.getId())
                                                                .amount(domain.getAmount())
                                                                .term(domain.getTerm())
                                                                .document(domain.getDocument())
                                                                .name(domain.getName())
                                                                .email(domain.getEmail())
                                                                .statusId(domain.getStatusId())
                                                                .interestRate(domain.getInterestRate())
                                                                .loanTypeId(domain.getLoanTypeId())
                                                                .loanTypeName(domain.getLoanTypeName())
                                                                .totalApprovedDebt(totalApprovedDebt) // <-
                                                                .build()
                                                )
                                , /*concurrency*/ Math.max(2, size)) // opcional: controla paralelismo
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

                    System.out.println("Solicitud actualizada localmente: " + actualizado);

                    return gateway1.update(actualizado)
                            .flatMap(saved ->
                                    gateway1.sendStatusUpdateMessage(saved)
                                            .thenReturn(saved)
                                            .onErrorResume(error -> {
                                                System.out.println("Error al enviar a SQS, se revertirá la solicitud");
                                                return gateway1.update(request) // rollback
                                                        .then(Mono.error(new RuntimeException("No se envio el mensaje a SQS el estado sigue siendo Pendiente", error)));
                                            })
                            );
                });
    }




}
