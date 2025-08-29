package co.com.requestloancrediyareactivo.model.requestloan.models;
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
    //private String id;
    private Double amount; //monto
    private Integer term; //plazo
    private String document;
    private String name; //nombre
    private String email; //email
    private Long statusId; //estado
    private Double interestRate; //tasa de interes
    private Long loanTypeId; //tipoprestamo
    private String loanTypeName;

}
