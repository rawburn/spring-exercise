package net.rawburn.reactive.annotation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author rawburn·rc
 */
@RestController
public class TestController {

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello World!");
    }
}
