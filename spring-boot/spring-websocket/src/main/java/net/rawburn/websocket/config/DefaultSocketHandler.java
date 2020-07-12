package net.rawburn.websocket.config;

/**
 * Spring 的 WebSocket 支持不依赖于 Spring MVC
 * WebSocketHandler 在 WebSocketHttpRequestHandler 的帮助下将其集成到其他 HTTP 服务环境中相对简单
 *
 * @author renchao
 * @see org.springframework.web.socket.server.support.WebSocketHttpRequestHandler
 * @see org.springframework.web.socket.WebSocketHandler
 * @since v1.0
 */

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class DefaultSocketHandler extends TextWebSocketHandler {

	private static final String TEST_FOR_SOCKET = "sessionId";

	private static CopyOnWriteArraySet<WebSocketSession> socketSessions = new CopyOnWriteArraySet<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		System.out.println("Connection established!");
		socketSessions.add(session);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		System.out.println("Received:" + message.getPayload());
	}

	/**
	 * Send message to client.
	 *
	 * @param sessionId the session ID
	 * @param message the message to send
	 */
	public void sendMessage(String sessionId, String message) {
		for (WebSocketSession socketSession : socketSessions) {
			// hit session
			Object id = socketSession.getAttributes().get(TEST_FOR_SOCKET);
			if (id.equals(sessionId)) {
			// if (socketSession.getId().equals(sessionId)) {
				try {
					if (socketSession.isOpen()) {
						socketSession.sendMessage(new TextMessage(message));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				socketSessions.remove(socketSession);
				break;
			}
		}
	}

}
