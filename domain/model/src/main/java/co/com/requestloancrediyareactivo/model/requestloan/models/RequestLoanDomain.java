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
    private String id;
    private Double amount;
    private Integer term;
    private String document;
    private String nombre;
    private String email;
    private Long statusId;
    private Long loanTypeId;
}
