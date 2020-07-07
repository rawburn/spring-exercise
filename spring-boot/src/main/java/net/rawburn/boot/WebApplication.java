package net.rawburn.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author rawburn·rc
 */
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @RestController
    static class TestController {

        @Autowired
        private MongoTemplate mongoTemplate;

        private static final Random RANDOM = new Random(47);

        @GetMapping("/user")
        public Map<String, Object> get() {
            int key = RANDOM.nextInt(1000000);
            System.out.println("Key: " + key);
            return mongoTemplate.findOne(query(where("key").is(key)), Map.class, "user");
        }
    }

    static class T {
        public static void main(String[] args) {
            int i = Runtime.getRuntime().availableProcessors();
            System.out.println(i); // 8
        }
    }
}
