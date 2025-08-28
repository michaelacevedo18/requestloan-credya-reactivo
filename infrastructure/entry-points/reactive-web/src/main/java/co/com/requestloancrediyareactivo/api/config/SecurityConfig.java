package co.com.requestloancrediyareactivo.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTSecurityContextRepository securityContextRepository;
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        //.pathMatchers(HttpMethod.POST, "/api/v1/solicitud").permitAll()
                        //.pathMatchers("/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud/registrar").hasRole("CUSTOMER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud/pendientes").hasRole("ASESOR")

                        //.pathMatchers(HttpMethod.GET, "/api/v1/solicitud/asesor").hasRole("ASESOR")
                        //.pathMatchers(HttpMethod.GET, "/api/v1/solicitud/customer").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )
                .build();
    }





    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
