package co.com.requestloancrediyareactivo.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("typeloan")
public class TypeLoanEntity {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("max_amount")
    private Double maxAmount;

    @Column("min_amount")
    private Double minAmount;

    @Column("interestrate")
    private Double interestrate;

    @Column("auto_validation")
    private Boolean auto_validation;
}
