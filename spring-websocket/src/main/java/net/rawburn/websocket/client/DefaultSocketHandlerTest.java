package net.rawburn.websocket.client;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

/**
 * @author renchao
 * @since v1.0
 */
@Component
public class DefaultSocketHandlerTest {

    private static final String WS_URI = "ws://localhost:8080/default?sessionId=BingGo";

    public static void main(String[] args) throws Exception {
        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setAsyncSendTimeout(0L);
        webSocketContainer.setDefaultMaxSessionIdleTimeout(0L);
        webSocketContainer.setDefaultMaxBinaryMessageBufferSize(0);
        webSocketContainer.setDefaultMaxTextMessageBufferSize(0);

        WebSocketClient client = new StandardWebSocketClient(webSocketContainer);
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, new MyHandler(), WS_URI);
        // manager.setAutoStartup(true);
        manager.start();

        System.in.read();
    }

    public static class MyHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            System.out.println("CONNECTED...");
            session.sendMessage(new TextMessage("HELLO, WEB SOCKET!"));
            super.afterConnectionEstablished(session);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message)
                throws Exception {
            System.out.println("RECEIVED: " + message.getPayload());
            super.handleTextMessage(session, message);
        }

        @Override
        protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
            super.handlePongMessage(session, message);
        }
    }

}