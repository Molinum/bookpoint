package cl.bookpoint.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * spring-cloud-gateway-server-webflux 4.3.5 ships without any CORS support
 * classes (GlobalCorsProperties, CorsGatewayFilterApplicationListener are
 * absent from that jar), so neither the globalcors YAML property nor a plain
 * CorsWebFilter/WebFluxConfigurer bean gets a chance to run before Gateway's
 * own routing answers OPTIONS preflight requests itself. Handling CORS here
 * directly, ordered first, sidesteps that gap.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsWebFilterConfig implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        String origin = request.getHeaders().getOrigin();
        if (origin != null) {
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            headers.add(HttpHeaders.VARY, "Origin");
        }

        if (request.getMethod() == HttpMethod.OPTIONS) {
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,PATCH,OPTIONS");
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800");
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }
}
