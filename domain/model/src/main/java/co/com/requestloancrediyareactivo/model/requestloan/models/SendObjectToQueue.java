package co.com.requestloancrediyareactivo.model.requestloan.models;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendObjectToQueue {
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
    private Double availableDebtCapacity;
}
