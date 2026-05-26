package ch.hearc.cafheg;

import ch.hearc.cafheg.infrastructure.persistence.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CafhegApplication {

    private static final Logger logger = LoggerFactory.getLogger(CafhegApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(CafhegApplication.class, args);
        startDatabase(ctx.getEnvironment());
        logger.info("Swagger UI available at http://localhost:8080/api/swagger-ui/index.html");
    }

    private static void startDatabase(Environment env) {
        String jdbcUrl = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (jdbcUrl == null || username == null || password == null) {
            throw new IllegalStateException(
                    "Database configuration is missing. Please check your application.yaml or environment " +
                            "variables.");
        }

        Database database = new Database();
        database.start(jdbcUrl, username, password);
    }

}
