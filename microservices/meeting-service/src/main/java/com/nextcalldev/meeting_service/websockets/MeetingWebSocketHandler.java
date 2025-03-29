package com.nextcalldev.meeting_service.websockets;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.nextcalldev.meeting_service.common.UserRole;
import com.nextcalldev.meeting_service.models.entities.UserSession;

@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {

    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public MeetingWebSocketHandler(NotificationWebSocketHandler notificationWebSocketHandler) {
	this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<UserSession>> meetings = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
	Long meetingId = getMeetingId(session);
	Long userId = getUserId(session);

	System.out.println("Conexión WebSocket establecida para el usuario " + userId + " en la reunión " + meetingId);

	meetings.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>())
		.add(new UserSession(userId, session, UserRole.PARTICIPANT));

	notificationWebSocketHandler.subscribeUserToMeetingNotifications(meetingId, session);
	notificationWebSocketHandler.sendNotification(meetingId, userId + " se ha unido a la reunión " + meetingId);
    }

    // Manejar mensajes entre los usuarios de la reunión
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	Long meetingId = getMeetingId(session);
	Long userId = getUserId(session);

	for (UserSession userSession : meetings.getOrDefault(meetingId, new CopyOnWriteArraySet<>())) {
	    if (userSession.getSession().isOpen() && !userSession.getSession().equals(session)) {
		userSession.getSession().sendMessage(message);
	    }
	}
    }

    // Manejar desconexiones de los usuarios
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
	Long meetingId = getMeetingId(session);
	Long userId = getUserId(session);

	meetings.getOrDefault(meetingId, new CopyOnWriteArraySet<>())
		.removeIf(userSession -> userSession.getSession().equals(session));

	if (meetings.get(meetingId).isEmpty()) {
	    meetings.remove(meetingId);
	}

	System.out.println("Usuario " + userId + " desconectado de la reunión: " + meetingId);
    }

    private Long getMeetingId(WebSocketSession session) {
	try {
	    String path = session.getUri().getPath();
	    System.out.println("Path: " + path);
	    String[] pathParts = session.getUri().getPath().split("/");
	    return Long.parseLong(pathParts[3]);
	} catch (Exception e) {
	    throw new IllegalArgumentException("ID de reunión no válido en la URI", e);
	}
    }

    private Long getUserId(WebSocketSession session) {
	try {
	    String path = session.getUri().getPath();
	    System.out.println("Path: " + path);
	    String[] pathParts = session.getUri().getPath().split("/");
	    return Long.parseLong(pathParts[4]);
	} catch (Exception e) {
	    throw new IllegalArgumentException("ID de usuario no válido en la URI", e);
	}
    }
}
