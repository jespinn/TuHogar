package co.edu.unbosque.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final SecretKey clave;
    private final long minutos;

    public JwtUtil(@Value("${app.jwt.secreto}") String secreto, @Value("${app.jwt.minutos}") long minutos) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // RNF-13: sin JWT_SECRETO se usa una clave aleatoria (las sesiones se pierden al reiniciar)
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.minutos = minutos;
    }

    public String generar(String correo, Collection<String> roles) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(correo)
                .claim("roles", List.copyOf(roles))
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + minutos * 60_000))
                .signWith(clave)
                .compact();
    }

    /** valida el token */
    public Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
