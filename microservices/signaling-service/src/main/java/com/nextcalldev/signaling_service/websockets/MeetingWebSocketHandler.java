package com.nextcalldev.signaling_service.websockets;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextcalldev.signaling_service.common.UserRole;
import com.nextcalldev.signaling_service.models.SignalMessageDto;
import com.nextcalldev.signaling_service.models.UserSession;


@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private MediaServerClient mediaServerClient;


    public MeetingWebSocketHandler(NotificationWebSocketHandler notificationWebSocketHandler, MediaServerClient mediaServerClient) {
	this.notificationWebSocketHandler = notificationWebSocketHandler;
	this.mediaServerClient = mediaServerClient;
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

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Long meetingId = getMeetingId(session);

        ObjectMapper mapper = new ObjectMapper();
        SignalMessageDto signal = mapper.readValue(message.getPayload(), SignalMessageDto.class);

        //Comprobamos si es un mensaje de WebRTC/media
        if (isMediaAction(signal.getType())) {
            try {
                String response = mediaServerClient.sendMessageAndWait(message.getPayload());

                if (response != null) {
                    session.sendMessage(new TextMessage(response));
                    System.out.println("Acción media enviada al media-server: " + signal.getType());
                } else {
                    session.sendMessage(new TextMessage("{\"error\":\"Sin respuesta del media-server\"}"));
                }
            } catch (Exception e) {
                session.sendMessage(new TextMessage("{\"error\":\"Error al comunicar con media-server\"}"));
                e.printStackTrace();
            }
            return;
        }

        for (UserSession userSession : meetings.getOrDefault(meetingId, new CopyOnWriteArraySet<>())) {
            if (userSession.getUserId().equals(signal.getTarget()) && userSession.getSession().isOpen()) {
                userSession.getSession().sendMessage(message);
            }
        }

//        System.out.println("Mensaje de tipo " + signal.getType() + " enviado de " + signal.getSender() + " a " + signal.getTarget());
    }


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
    
    private boolean isMediaAction(String type) {
	    return Set.of(
	        "get-rtp-capabilities",
	        "create-transport",
	        "connect-transport",
	        "produce",
	        "consume"
	    ).contains(type);
	}
}