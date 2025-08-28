package co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.PageDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IRequestLoanUseCase {
    Mono<RequestLoanDomain> apply(RequestLoanDomain requestLoanD, UserResponseDomain user);
    Mono<PageDTO<RequestLoanDomain>> findPendingForReview(List<Long> statuses, int page, int size);
}
