package s_a_rb01_its6.gateway.routes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Routes {
    //define routes
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${post.service.url}")
    private String postServiceUrl;

    @Bean
    public RouteLocator userServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                //user registraton route
                .route("user-register", r -> r
                        .path("/api/v1/user/register")
                        .uri(userServiceUrl)
                )
                // user service route with authorization
                .route("user-service", r -> r
                        .path("/api/v1/user/**")
                        .uri(userServiceUrl)
                )
                .route("post-service", r -> r
                        .path("/api/v1/post/**")
                        .uri(postServiceUrl)
                ).build();

        //TODO expand routes for other microservices.
    }
}
