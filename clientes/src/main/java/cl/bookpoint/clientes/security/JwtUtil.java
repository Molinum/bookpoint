package cl.bookpoint.clientes.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Long clienteId, String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(clienteId))
                .claim("email", email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    /**
     * Valida la firma y vigencia del token y retorna el id del cliente (subject).
     * Lanza JwtException (o una subclase) si el token es invalido, esta expirado o fue alterado.
     */
    public Long validarYObtenerClienteId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
