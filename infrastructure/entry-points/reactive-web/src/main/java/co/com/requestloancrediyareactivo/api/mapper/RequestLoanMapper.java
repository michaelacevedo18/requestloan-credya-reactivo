package co.com.requestloancrediyareactivo.api.mapper;

import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;

public class RequestLoanMapper {
    public static RequestLoanDomain toDomain(RequestLoanCreateDTO dto) {
        return RequestLoanDomain.builder()
                //.id(dto.id()) // puede ser null, se generará UUID en el adaptador
                .amount(dto.amount())
                .document(dto.document())
                .term(dto.term())
                .loanTypeId(dto.loanTypeId())
                //.statusId(dto.statusId())
                .email(dto.email())
                .nombre(dto.nombre())
                .build();
    }

    // De Dominio a DTO
    public static RequestLoanCreateDTO toDTO(RequestLoanDomain requestLoan) {
        return RequestLoanCreateDTO.builder()
                //.id(requestLoan.getId())
                .document(requestLoan.getDocument())
                .amount(requestLoan.getAmount())
                .term(requestLoan.getTerm())
                .nombre(requestLoan.getNombre())
                .loanTypeId(requestLoan.getLoanTypeId())
                //.statusId(requestLoan.getStatusId())
                .email(requestLoan.getEmail())
                .build();
    }
}
