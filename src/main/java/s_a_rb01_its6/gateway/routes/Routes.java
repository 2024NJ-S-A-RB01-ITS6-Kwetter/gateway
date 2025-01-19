package s_a_rb01_its6.gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class Routes {
    // Define routes
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${post.service.url}")
    private String postServiceUrl;

    @Bean
    public RouteLocator userServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User registration route
                .route("user-register", r -> r
                        .path("/api/v1/user/register")
                        .uri(userServiceUrl)
                )
                // User service route with retry filter
                .route("user-service", r -> r
                        .path("/api/v1/user/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(3) // Number of retries
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)) // Retry for specific statuses
                                .filter(handleConnectException())) // Apply exception handler
                        .uri(userServiceUrl)
                )
                // Post service route with retry filter
                .route("post-service", r -> r
                        .path("/api/v1/post/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE))
                                .filter(handleConnectException())) // Apply exception handler
                        .uri(postServiceUrl)
                )
                .route("follow-service", r -> r
                        .path("/api/v1/follow/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE))
                                .filter(handleConnectException())) // Apply exception handler
                        .uri(postServiceUrl)
                )
                .build();
    }

    private GatewayFilter handleConnectException() {
        return (exchange, chain) -> chain.filter(exchange).onErrorResume(e -> {
            if (e instanceof java.net.ConnectException) {
                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        });
    }
}