package net.rawburn.websocket.config;

import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.standard.SpringConfigurator;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;

import javax.websocket.server.ServerEndpoint;

/**
 * 自定义初始 HTTP WebSocket 握手请求的最简单方法是通过一个 HandshakeInterceptor，
 * 它暴露握手方法的“之前”和“之后”这样的拦截器可用于阻止握手或使任何属性可用于
 * WebSocketSession。例如，有一个内置拦截器，用于将 HTTP 会话属性传递给 WebSocket 会话
 *
 * @author renchao
 * @since v1.0
 * @see WebSocketHandler
 * @see HandshakeHandler
 * @see WebSocketHandlerDecorator
 * @see HandshakeInterceptor
 * @see WebSocketHttpRequestHandler
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(defaultHandler(), "/default")
		        .addInterceptors(new DefaultHandshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler());
    }

	/**
	 * @see WsWebSocketContainer
	 */
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192);
		container.setMaxBinaryMessageBufferSize(8192);
		return container;
	}

    @Bean
    public WebSocketHandler defaultHandler() {
        return new DefaultSocketHandler();
    }

}