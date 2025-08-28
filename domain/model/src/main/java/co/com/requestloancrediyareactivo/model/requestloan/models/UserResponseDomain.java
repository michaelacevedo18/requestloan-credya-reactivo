package co.com.requestloancrediyareactivo.model.requestloan.models;
import lombok.*;

@Data
@Builder
public class UserResponseDomain {
    private String token;
    private String email;
    private String idNumber;
    private String rolName;
}
