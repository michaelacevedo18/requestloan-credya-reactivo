package co.com.requestloancrediyareactivo.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
        import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "DTO para crear una solicitud de préstamo")
public record RequestLoanCreateDTO(
        //String id,

        @NotBlank(message = "El documento es obligatorio")
        @Schema(description = "documento del cliente", example = "1049049049", required = true)
        String document,

        @Email(message = "Debe ser un correo válido")
        @NotBlank(message = "El correo es obligatorio")
        @Schema(description = "Correo del cliente", example = "cliente@correo.com", required = true)
        String email,


        @NotBlank(message = "El correo es obligatorio")
        @Schema(description = "Nombre", example = "Julian Perez", required = true)
        String nombre,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor que 0")
        @DecimalMax(value = "15000000.0", message = "El salario no debe superar 15 millones")
        @Schema(description = "Monto solicitado", example = "1500000", required = true)
        Double amount,

        @NotNull(message = "El término del prestamo obligatorio")
        @Schema(description = "Plazo del préstamo en meses", example = "6", required = true)
        Integer term,

        //@NotNull(message = "El estado es obligatorio")
        //Long statusId,

        @NotNull(message = "El tipo de prestamo es obligatorio")
        @Schema(description = "ID del tipo de préstamo, debe ser válido", example = "1", required = true)
        Long loanTypeId
) {}