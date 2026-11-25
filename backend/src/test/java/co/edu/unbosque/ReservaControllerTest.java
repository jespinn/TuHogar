package co.edu.unbosque;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** solo administrador */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ReservaControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String tokenDe(String correo, String contrasena) throws Exception {
        String cuerpo = "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
        String respuesta = mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("token").asText();
    }

    @Test
    void elAdministradorVeLasDiecinueveReservasDeEjemplo() throws Exception {
        String token = tokenDe("admin@alojamientos.co", "Admin1234");

        mvc.perform(get("/api/reservas/todas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(19))
                .andExpect(jsonPath("$[0].anfitrion").exists())
                .andExpect(jsonPath("$[0].huesped").exists());
    }

    @Test
    void losFiltrosQuitanLasReservasQueNoCumplen() throws Exception {
        String token = tokenDe("admin@alojamientos.co", "Admin1234");

        mvc.perform(get("/api/reservas/todas?estado=CONFIRMADA").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].estado").value("CONFIRMADA"));
        mvc.perform(get("/api/reservas/todas?publicacion=999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unHuespedNoPuedeVerTodasLasReservas() throws Exception {
        String token = tokenDe("maria.huesped@correo.co", "Clave1234");

        mvc.perform(get("/api/reservas/todas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void elComprobanteSeDescargaEnPdf() throws Exception {
        // reserva de maría
        String maria = tokenDe("maria.huesped@correo.co", "Clave1234");
        mvc.perform(get("/api/reservas/5/comprobante").header("Authorization", "Bearer " + maria))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // administrador sin permiso
        String admin = tokenDe("admin@alojamientos.co", "Admin1234");
        mvc.perform(get("/api/reservas/5/comprobante").header("Authorization", "Bearer " + admin))
                .andExpect(status().isForbidden());
    }
}
