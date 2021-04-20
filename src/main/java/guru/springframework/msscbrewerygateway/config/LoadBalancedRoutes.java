package guru.springframework.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.ServerCodecConfigurer;

@Profile("local-discovery")
@Configuration
public class LoadBalancedRoutes {

    //this bug was because of spring-boot-starter-web dependency
    // from parent BOM, that is not compatible with eureka
//    @Bean
//    public ServerCodecConfigurer serverCodecConfigurer() {
//        return ServerCodecConfigurer.create();
//    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/v1/beer*", "/api/v1/beer/*")
//                        .filters(f -> f.rewritePath("/api/v1/beer(?<segment>/?.*)", "/api/v1/beer${segment}"))
                        .uri("lb://BEER-SERVICE")
                        .id("beer-service"))
                .route(r -> r.path("/api/v1/customers/**")
                        .uri("lb://beer-order-service")
                        .id("order-service"))
                .route(r -> r.path("/api/v1/beer/*/inventory")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("inventoryCB")
                                .setFallbackUri("forward:/inventory-failover")
                                .setRouteId("inv-failover")
                        ))
                        .uri("lb://beer-inventory-service")
                        .id("inventory-service"))
                .route(r -> r.path("/inventory-failover/**")
                        .uri("lb://inventory-failover")
                        .id("inventory-failover-service"))
                .build();
    }
}
