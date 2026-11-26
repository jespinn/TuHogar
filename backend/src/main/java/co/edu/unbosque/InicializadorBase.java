package co.edu.unbosque;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * crea el esquema si no existe y carga los datos de ejemplo solo cuando la base esta vacia.
 * se activa con DB_INICIALIZAR=true (Render), asi se puede reiniciar sin duplicar datos.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.db.inicializar", havingValue = "true")
public class InicializadorBase implements ApplicationRunner {
    private final DataSource dataSource;

    public InicializadorBase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        ejecutar("schema.sql");
        Integer roles = new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM rol", Integer.class);
        if (roles == null || roles == 0) {
            ejecutar("data.sql");
        }
    }

    private void ejecutar(String archivo) {
        ResourceDatabasePopulator populador = new ResourceDatabasePopulator(new ClassPathResource(archivo));
        populador.setSqlScriptEncoding("UTF-8");
        populador.execute(dataSource);
    }
}
