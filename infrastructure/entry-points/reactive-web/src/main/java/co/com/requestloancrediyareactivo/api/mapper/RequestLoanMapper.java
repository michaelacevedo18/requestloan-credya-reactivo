package co.com.requestloancrediyareactivo.api.mapper;

import co.com.requestloancrediyareactivo.api.dtos.RequestLoanCreateDTO;
import co.com.requestloancrediyareactivo.model.requestloan.models.RequestLoanDomain;

public class RequestLoanMapper {
    public static RequestLoanDomain toDomain(RequestLoanCreateDTO dto) {
        System.out.println("-------Evaluando mapper 1: "+ dto.version());
        return RequestLoanDomain.builder()
                .id(dto.id()) // puede ser null, se generará UUID en el adaptador
                .amount(dto.amount())
                .document(dto.document())
                .term(dto.term())
                .loanTypeId(dto.loanTypeId())
                //.statusId(dto.statusId())
                //.interestRate(dto.interesRate())
                //.interestRate(3.3D)
                .version(dto.version())
                .email(dto.email())
                .name(dto.name())
                .build();
    }

    // De Dominio a DTO
    public static RequestLoanCreateDTO toDTO(RequestLoanDomain requestLoan) {
        System.out.println("-------Evaluando mapper 2");
        return RequestLoanCreateDTO.builder()
                .id(requestLoan.getId())
                .document(requestLoan.getDocument())
                .amount(requestLoan.getAmount())
                .term(requestLoan.getTerm())
                .name(requestLoan.getName())
                .loanTypeId(requestLoan.getLoanTypeId())
                .version(requestLoan.getVersion())
                //.interesRate(requestLoan.getInterestRate())
                //.statusId(requestLoan.getStatusId())
                .email(requestLoan.getEmail())
                .build();
    }
}
