package co.com.requestloancrediyareactivo.api.dtos;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private List<String> details;
    private int statusCode;
    private String error;

    private LocalDateTime timestamp;
    private String path;
    private T data;
}
