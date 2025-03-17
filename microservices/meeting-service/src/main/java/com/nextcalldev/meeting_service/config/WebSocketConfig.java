package com.nextcalldev.meeting_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.nextcalldev.meeting_service.websockets.MeetingWebSocketHandler;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MeetingWebSocketHandler meetingWebSocketHandler;

    public WebSocketConfig(MeetingWebSocketHandler meetingWebSocketHandler) {
        this.meetingWebSocketHandler = meetingWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meetingWebSocketHandler, "/ws/meeting/{meetingId}")
                .setAllowedOrigins("*");
    }
}