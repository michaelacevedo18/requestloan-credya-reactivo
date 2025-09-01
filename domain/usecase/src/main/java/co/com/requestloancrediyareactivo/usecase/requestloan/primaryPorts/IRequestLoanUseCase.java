package co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface IRequestLoanUseCase {
    Mono<RequestLoanDomain> apply(RequestLoanDomain requestLoanD, UserResponseDomain user);
    Mono<PageDTO<RequestLoanDomain>> findPendingForReview(List<Long> statuses, int page, int size);
    Mono<RequestLoanDomain> updateStatus(UUID id, int status, String comentario);
}
