package net.rawburn.reactive.annotation.socket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EchoHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        Flux<WebSocketMessage> socketMessageFlux = session.receive()
                .map(msg -> session.textMessage("ECHO -> " + msg.getPayloadAsText()));
        return session.send(socketMessageFlux);
    }
}