package org.example.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器 以身份认证为例
 */
@Component
@Order(-1)
public class AuthorizeFilter implements GlobalFilter {
    /**
     * 全局过滤器 判断是否含有authorization参数且参数值为admin
     * 是 放行给下一个过滤器
     * 否 拦截并返回401
     * @param exchange 请求上下文 可获取Request Response等信息
     * @param chain    过滤器链中的下一个过滤器
     * @return 标志当前过滤器业务结束
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> params = request.getQueryParams();
        String auth = params.getFirst("authorization");
        if ("admin".equals(auth)) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
