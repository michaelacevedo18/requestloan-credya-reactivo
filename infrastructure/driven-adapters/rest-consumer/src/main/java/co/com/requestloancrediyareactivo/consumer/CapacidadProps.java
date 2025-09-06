package co.com.requestloancrediyareactivo.consumer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.capacidad")
@Data
public class CapacidadProps {
    private String baseUrl;
    private Paths paths;

    @Data public static class Paths {
        private String salary;
        private String calcular;
    }
}
