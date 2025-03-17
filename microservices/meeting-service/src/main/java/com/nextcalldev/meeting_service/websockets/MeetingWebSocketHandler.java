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

// TextWebSocketHandler → Nos permite manejar mensajes de texto en WebSockets.
@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {

	// Almacenar sesiones por reunión
	private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> meetings = new ConcurrentHashMap<>();

	// Manejar nuevas conexiones WebSocket
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		// Cuando un usuario se conecta, obtenemos el meetingId desde la URL.
		String meetingId = getMeetingId(session);
		meetings.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>())
				.add(session);
		System.out.println("Usuario conectado a la reunión: " + meetingId);
	}

	// Manejar mensajes entre los usuarios de la reunión
	// Se ejecuta cuando un usuario envía un mensaje.
	@Override
	protected void handleTextMessage(WebSocketSession session,
			TextMessage message) throws IOException {
		String meetingId = getMeetingId(session);
		for (WebSocketSession s : meetings.getOrDefault(meetingId,
				new CopyOnWriteArraySet<>())) {
			// Si la conexion esta activa envie el mensaje
			if (s.isOpen()) {
				s.sendMessage(message);
			}
		}
	}

	// Manejar desconexiones de los usuarios
	@Override
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) {
		String meetingId = getMeetingId(session);
		if (meetings.get(meetingId).isEmpty()) {
			meetings.remove(meetingId);
		}
		meetings.getOrDefault(meetingId, new CopyOnWriteArraySet<>())
				.remove(session);
		System.out.println("Usuario desconectado de la reunión: " + meetingId);
	}

	private String getMeetingId(WebSocketSession session) {
		return session.getUri().getPath().split("/ws/meeting/")[1];
	}
}
