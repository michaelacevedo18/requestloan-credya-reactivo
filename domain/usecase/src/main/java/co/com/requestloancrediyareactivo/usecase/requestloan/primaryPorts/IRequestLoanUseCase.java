package co.com.requestloancrediyareactivo.usecase.requestloan.primaryPorts;

import co.com.requestloancrediyareactivo.model.requestloan.models.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface IRequestLoanUseCase {
    Mono<RequestLoanDomain> crearr(RequestLoanDomain requestLoanD, UserResponseDomain user);
     Mono<PageDTO<PendingRequestViewDTO>> findPendingForReview(List<Long> statuses, int page, int size);
    Mono<RequestLoanDomain> updateStatus(UUID id, int status, BigDecimal calculatedMontlyFee);
    Mono<RequestLoanDomain> updateStatusAtomQuot(UUID id, int status, int atomVersion, double quotLoan);
    Mono<SendObjectToQueue> getPropsToQueue(UUID id, BigInteger documentNumber);
}
