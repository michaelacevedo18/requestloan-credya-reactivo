package co.com.requestloancrediyareactivo.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Solicitudes de Prestamo",
                version = "1.0",
                description = "Documentación de endpoints para gestionar solicitudes de crédito",
                contact = @Contact(name = "Michael Acevedo", email = "michaelacevedoruiz48@gmail.com")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Servidor Local")
)
public class OpenApiConfig {
}
