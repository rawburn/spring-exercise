package net.rawburn.reactive;

import com.mongodb.connection.ClusterSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Random;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author rawburn·rc
 *
 * @see Schedulers
 * @see ClusterSettings.Builder
 * @see NettyReactiveWebServerFactory
 */
@SpringBootApplication
public class WebFluxApplication {

    public static void main(String[] args) {
        // System.setProperty("reactor.schedulers.defaultPoolSize", "30");
        SpringApplication.run(WebFluxApplication.class, args);
    }

    @RestController
    static class UserController {

        @Autowired
        private ReactiveMongoTemplate reactiveMongoTemplate;

        private static final Random RANDOM = new Random(47);

        @GetMapping("/user")
        public Mono<Map> find() {
            int key = RANDOM.nextInt(1000000);
            System.out.println("Key: " + key);
            return reactiveMongoTemplate.findOne(
                    query(where("key").is(key)), Map.class, "user");
        }

        @GetMapping("/users")
        public Flux<Map> findAll() {
            return reactiveMongoTemplate.findAll(Map.class);
        }
    }
}
