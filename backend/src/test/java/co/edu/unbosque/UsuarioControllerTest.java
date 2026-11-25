package co.edu.unbosque;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unbosque.usuarios.repository.CodigoRecuperacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class UsuarioControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CodigoRecuperacionRepository codigos;

    private String registroJson(String correo, String documento, String contrasena) {
        return """
            {"nombre":"Prueba Uno","correo":"%s","contrasena":"%s","confirmacion":"%s","tipoDocumento":"CC",
             "numeroDocumento":"%s","roles":["HUESPED"],"aceptaTratamientoDatos":true}
            """.formatted(correo, contrasena, contrasena, documento);
    }

    private String tokenDe(String correo, String contrasena) throws Exception {
        String cuerpo = """
            {"correo":"%s","contrasena":"%s"}""".formatted(correo, contrasena);
        String respuesta = mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("token").asText();
    }

    @Test
    void registroValidoCreaLaCuentaSinExponerContrasena() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("ctrl1@correo.co", "9001", "Clave1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("ctrl1@correo.co"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist());
    }

    @Test
    void registroInvalidoDevuelveErrorConCampo() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("correo-malo", "9002", "Clave1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.campo").value("correo"));
    }

    @Test
    void loginCorrectoDevuelveTokenYUsuarioSinHash() throws Exception {
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@alojamientos.co\",\"contrasena\":\"Admin1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.roles[0]").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.contrasenaHash").doesNotExist());
    }

    @Test
    void recuperarContrasenaPermiteEntrarConLaNueva() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                .content(registroJson("ctrl2@correo.co", "9003", "Clave1234"))).andExpect(status().isCreated());

        mvc.perform(post("/api/usuarios/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ctrl2@correo.co\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").isNotEmpty()); // código en pantalla
        var guardados = codigos.findAll();
        String codigo = guardados.get(guardados.size() - 1).getCodigo();

        mvc.perform(post("/api/usuarios/restablecer").contentType(MediaType.APPLICATION_JSON).content("""
                {"correo":"ctrl2@correo.co","codigo":"%s","contrasena":"NuevaClave1","confirmacion":"NuevaClave1"}
                """.formatted(codigo))).andExpect(status().isOk());

        tokenDe("ctrl2@correo.co", "NuevaClave1"); // falla si la nueva contraseña no sirve
    }

    @Test
    void corsPermiteAlFrontendDeAngular() throws Exception {
        mvc.perform(options("/api/usuarios/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void reportesRespondeSegunElRol() throws Exception {
        mvc.perform(get("/api/reportes/indicadores")).andExpect(status().isUnauthorized());

        String tokenHuesped = tokenDe("maria.huesped@correo.co", "Clave1234");
        mvc.perform(get("/api/reportes/indicadores").header("Authorization", "Bearer " + tokenHuesped))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));

        mvc.perform(get("/api/usuarios/perfil").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void soloElAdministradorGestionaCuentas() throws Exception {
        String anfitrion = tokenDe("laura.anfitriona@correo.co", "Clave1234");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + anfitrion))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/2/desactivar").header("Authorization", "Bearer " + anfitrion))
                .andExpect(status().isForbidden());

        String admin = tokenDe("admin@alojamientos.co", "Admin1234");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("admin@alojamientos.co"));
    }
}
