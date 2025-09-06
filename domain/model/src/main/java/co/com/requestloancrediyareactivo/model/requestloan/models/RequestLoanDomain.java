package co.com.requestloancrediyareactivo.model.requestloan.models;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RequestLoanDomain {
    private UUID id;
    private Double amount; //monto
    private Integer term; //plazo
    private String document;
    private String name; //nombre
    private String email; //email
    private Long statusId; //estado
    private Double loanTypeInterestrate;
    private Boolean auto_validation;
    private Long loanTypeId; //tipoprestamo
    private String loanTypeName;
    private String comment;
    private Double baseSalary;
    private Double totalMonthlyDebtApproved;
}
