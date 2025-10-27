package model;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

public class TokenUtil {
    public static String generateToken(String username, String role) {
        Algorithm algorithm = Algorithm.HMAC256("secret"); // Use a strong secret!
        String token = JWT.create()
            .withSubject(username)
            .withClaim("role",role)
            .sign(algorithm);
        return token;
    }

    public static DecodedJWT decodeToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256("secret"); // Use the same secret!
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token); // Throws exception if invalid
    }

    public static boolean isTokenValido(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            // A lógica de validação é a mesma do decodeToken
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token); // Apenas tenta verificar. Se não lançar exceção, é válido.
            return true;
        } catch (JWTVerificationException exception){
            // Se entrar aqui, o token é inválido (assinatura errada, expirado, etc.)
            return false;
        }
    }
}