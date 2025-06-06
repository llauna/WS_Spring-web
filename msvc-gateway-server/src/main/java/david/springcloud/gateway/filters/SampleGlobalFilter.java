package david.springcloud.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        logger.info("ejecutando el filtro antes del request PRE");

        ServerHttpRequest mutateRequest = exchange.getRequest()
                .mutate()
                .headers(h-> h.add("token", "llaunas!!!"))
                .build();
        ServerWebExchange newExchange = exchange.mutate()
                .request(mutateRequest).build();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("ejecutando filtro POST response");
            String tokenRecuperado = newExchange.getRequest().getHeaders().getFirst("token");
            if (tokenRecuperado != null) {
                logger.info("token recuperado del request: " + tokenRecuperado);
                exchange.getResponse().getHeaders().add("token", tokenRecuperado);
            }
            else {
                logger.warn("El header 'token no se encontró en el request en la fase POST.");
            }

            Optional.ofNullable(tokenRecuperado)
                    .ifPresent(value -> {
                        logger.info("token2 (basado en token recuperado): " + value);
                        exchange.getResponse().getHeaders().add("token2", String.valueOf(value));
                    });

            exchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            //exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }

        @Override
        public int getOrder() {
        return 100;
    }
}
