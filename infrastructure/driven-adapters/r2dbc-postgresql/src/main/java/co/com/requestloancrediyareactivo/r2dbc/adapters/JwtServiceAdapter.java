package co.com.requestloancrediyareactivo.r2dbc.adapters;

import co.com.requestloancrediyareactivo.model.requestloan.gateways.ports.JWTServicePort;
import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;



@Component
public class JwtServiceAdapter implements JWTServicePort {
    private static final String SECRET_KEY = "my-super-secret-key-for-jwt-signing-which-must-be-long";

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Override
    public UserResponseDomain validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UserResponseDomain.builder()
                .email(claims.getSubject())
                .idNumber(claims.get("idNumber").toString())
                .rolName(claims.get("rol").toString())
                .build();
    }
}
