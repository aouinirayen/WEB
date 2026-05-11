package tn.esprit.pidev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PIDEV API")
                        .version("1.0.0")
                        .description("API documentation for PIDEV Project")
                        .contact(new Contact()
                                .name("PIDEV Team")
                                .email("support@pidev.com")))
                .addServersItem(new Server()
                        .url("http://localhost:8081")
                        .description("Local Server"));
    }
}

