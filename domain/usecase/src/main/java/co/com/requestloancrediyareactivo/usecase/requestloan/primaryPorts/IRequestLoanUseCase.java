package co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import reactor.core.publisher.Mono;

public interface IRequestLoanUseCase {
    Mono<RequestLoanDomain> apply(RequestLoanDomain requestLoanD, UserResponseDomain user);
}
