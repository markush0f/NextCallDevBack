package com.nextcalldev.signaling_service.websockets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Configuration
public class CorsWebSocketHandlerDecoratorFactory {
    @Bean
    public WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory() {
        return handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session)
                    throws Exception {
                super.afterConnectionEstablished(session);
            }
        };
    }
}
