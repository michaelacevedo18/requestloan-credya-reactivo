package co.com.requestloancrediyareactivo.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "DTO para crear una solicitud de préstamo")
public record RequestLoanUpdateDTO(
        @NotNull(message = "El id no puede ser nulo")
        @Schema(description = "id de la solicitud", example = "96942fc4-65b9-44da-b29e-a836fb6f4d32", required = true)
        UUID id,

        @NotNull(message = "El id del estadoes obligatorio")
        @Schema(description = "El id del estado de la solicitud, debe ser valido", example = "2, 3, 4", required = true)
        int statusId,

        @NotBlank(message = "El correo es obligatorio")
        @Schema(description = "Comentario", example = "Aceptación y/o recomendaciones", required = true)
        String comment


) {}