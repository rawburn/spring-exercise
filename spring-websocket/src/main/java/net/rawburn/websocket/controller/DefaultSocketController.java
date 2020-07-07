package net.rawburn.websocket.controller;

import net.rawburn.websocket.config.DefaultSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * @author renchao
 * @since v1.0
 */
@RestController
public class DefaultSocketController {

    @Autowired
    private DefaultSocketHandler handler;

    // @Autowired
    // private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("notice")
    public String notice(String sessionId) {
        handler.sendMessage(sessionId, String.format("Current Time: %s", Instant.now()));
        return "SUCCESS";
    }

    // @GetMapping
    // public String notice2(String name) {
    //     simpMessagingTemplate.convertAndSendToUser(name, "/notice",
    //             String.format("Current Time: %s", Instant.now()));
    //     return "SUCCESS";
    // }

}
