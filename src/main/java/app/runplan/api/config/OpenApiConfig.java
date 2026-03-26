package app.runplan.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "RunPlan API", version = "1.0.0",
                description = "API pour alimenter RunPlan (entraînement, sommeil, poids, stats hebdo)."),
        servers = {
                @Server(url = "https://api.runplan.app/v1", description = "Production"),
                @Server(url = "http://localhost:3000/v1", description = "Local")
        },
        tags = {
                @Tag(name = "Profile"), @Tag(name = "Weeks"), @Tag(name = "Sessions"),
                @Tag(name = "Sleep"), @Tag(name = "Weight"), @Tag(name = "Stats")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
