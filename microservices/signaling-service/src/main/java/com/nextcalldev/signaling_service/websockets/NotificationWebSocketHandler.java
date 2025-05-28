package com.nextcalldev.signaling_service.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> meetingNotifications = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        meetingNotifications.forEach((meetingId, sessions) -> {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                meetingNotifications.remove(meetingId);
            }
        });
        System.out.println("Conexión cerrada para notificaciones: " + session.getId());
    }

    public void subscribeUserToMeetingNotifications(Long meetingId, WebSocketSession session) {
        meetingNotifications.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("Usuario suscrito a notificaciones de la reunión: " + meetingId);
    }

    public void sendNotification(Long meetingId, String message) {
        CopyOnWriteArraySet<WebSocketSession> subscribers = meetingNotifications.get(meetingId);
        if (subscribers != null) {
            for (WebSocketSession session : subscribers) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}