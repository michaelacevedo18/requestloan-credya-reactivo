package co.com.requestloancrediyareactivo.model.requestloan.gateways.ports;

import co.com.requestloancrediyareactivo.model.requestloan.models.UserResponseDomain;

public interface JWTServicePort {
    UserResponseDomain validateToken(String token);
}
