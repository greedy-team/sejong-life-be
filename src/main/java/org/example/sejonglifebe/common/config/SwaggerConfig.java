package org.example.sejonglifebe.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Server");

        Server devServer = new Server();
        devServer.setUrl("http://3.37.62.143:8081");
        devServer.setDescription("Development Server");

        Server apiServer = new Server();
        apiServer.setUrl("https://sejong-life.site");
        apiServer.setDescription("Production Server");

        return new OpenAPI()
                .info(new Info()
                        .title("SejongLife API")
                        .version("1.0")
                        .description("SejongLife API v1"))
                .servers(Arrays.asList(localServer, apiServer));
    }
}
