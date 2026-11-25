package co.edu.unbosque.seguridad;

import co.edu.unbosque.comun.ErrorApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final ObjectMapper json;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper json) {
        this.jwtUtil = jwtUtil;
        this.json = json;
    }

    @Bean
    SecurityFilterChain configurarSeguridad(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(rutas -> rutas
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/usuarios/registro", "/api/usuarios/login", "/api/usuarios/recuperar", "/api/usuarios/reactivar",
                        "/api/usuarios/restablecer").permitAll()
                // cuentas: solo administrador
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/*/desactivar", "/api/usuarios/*/activar").hasRole("ADMINISTRADOR")
                // alojamientos públicos
                .requestMatchers(HttpMethod.GET, "/api/reservas/alojamientos", "/api/reservas/alojamientos/*").permitAll()
                .requestMatchers("/api/reportes/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/reservas/todas").hasRole("ADMINISTRADOR") // CU-27
                // permisos de reservas
                .requestMatchers("/api/reservas/cotizar", "/api/reservas/mias",
                        "/api/reservas/*/pagar", "/api/reservas/*/cancelar", "/api/reservas/*/devolucion").hasRole("HUESPED")
                .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole("HUESPED")
                .requestMatchers("/api/reservas/*/comprobante").hasAnyRole("HUESPED", "ANFITRION") // CU-30
                .requestMatchers("/api/reservas/de-mis-publicaciones", "/api/reservas/*/aprobar",
                        "/api/reservas/*/rechazar").hasRole("ANFITRION")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escribirError(res, 401, "NO_AUTENTICADO", "Debe iniciar sesión o su sesión venció"))
                .accessDeniedHandler((req, res, ex) ->
                        escribirError(res, 403, "SIN_PERMISO", "No tiene permiso para esta acción")))
            .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder crearCodificador() {
        return new BCryptPasswordEncoder();
    }

    // orígenes permitidos
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origenes.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    private void escribirError(HttpServletResponse res, int estado, String codigo, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getWriter(), new ErrorApi(codigo, mensaje, null));
    }
}
