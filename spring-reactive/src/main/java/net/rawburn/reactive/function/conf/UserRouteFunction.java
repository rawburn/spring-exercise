package net.rawburn.reactive.function.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author rawburn·rc
 */
@Configuration
public class UserRouteFunction {

    private static final Random RANDOM = new Random(47);

    @Bean
    RouterFunction<ServerResponse> composedRoutes(ReactiveMongoTemplate reactiveMongoTemplate) {
        Mono<Map> one = reactiveMongoTemplate.findOne(
                query(where("key").is(RANDOM.nextInt(1000000))), Map.class, "user");
        return route(GET("/user"), req -> ServerResponse.ok().body(one, Map.class));
    }
}
