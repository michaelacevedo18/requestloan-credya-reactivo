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
public class StateLoanDomain {
    private Long id;
    private String name;
    private String description;
}
