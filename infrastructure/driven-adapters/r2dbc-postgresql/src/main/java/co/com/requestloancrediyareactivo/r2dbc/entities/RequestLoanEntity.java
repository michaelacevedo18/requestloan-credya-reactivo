package co.com.requestloancrediyareactivo.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("requestloan")
public class RequestLoanEntity {
    @Id
    private String id;

    @Column("document")
    private String document;

    @Column("amount")
    private Double amount;

    @Column("term")
    private Integer term;

    @Column("email")
    private String email;

    @Column("status_id")
    private Long statusId;

    @Column("loan_type_id")
    private Long loanTypeId;
}
