package co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface IRequestLoanUseCase {
    Mono<RequestLoanDomain> apply(RequestLoanDomain requestLoanD, UserResponseDomain user);
     Mono<PageDTO<PendingRequestViewDTO>> findPendingForReview(List<Long> statuses, int page, int size);
    Mono<RequestLoanDomain> updateStatus(UUID id, int status, String comentario);
    Mono<SendObjectToQueue> getPropsToQueue(UUID id, BigInteger documentNumber);
}
