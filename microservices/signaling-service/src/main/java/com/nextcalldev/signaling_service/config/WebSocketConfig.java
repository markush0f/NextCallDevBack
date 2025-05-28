package com.nextcalldev.signaling_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.nextcalldev.signaling_service.websockets.MeetingWebSocketHandler;
import com.nextcalldev.signaling_service.websockets.NotificationWebSocketHandler;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final MeetingWebSocketHandler meetingWebSocketHandler;
	private final NotificationWebSocketHandler notificationWebSocketHandler;

	public WebSocketConfig(MeetingWebSocketHandler meetingWebSocketHandler,
			NotificationWebSocketHandler notificationWebSocketHandler) {
		this.meetingWebSocketHandler = meetingWebSocketHandler;
		this.notificationWebSocketHandler = notificationWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(meetingWebSocketHandler, "/ws/meeting/{meetingId}/{userId}")
				.setAllowedOrigins("*");
		registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
				.setAllowedOrigins("*");
	}
}