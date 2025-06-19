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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nextcalldev.signaling_service.common.UserRole;
import com.nextcalldev.signaling_service.models.NotificationMessageDto;
import com.nextcalldev.signaling_service.models.SignalMessageDto;
import com.nextcalldev.signaling_service.models.UserSession;


@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final MediaServerClient mediaServerClient;


    public MeetingWebSocketHandler(NotificationWebSocketHandler notificationWebSocketHandler, MediaServerClient mediaServerClient) {
	this.notificationWebSocketHandler = notificationWebSocketHandler;
	this.mediaServerClient = mediaServerClient;
    }

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<UserSession>> meetings = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
	Long meetingId = getMeetingId(session);
	Long userId = getUserId(session);
	System.out.println("Cliente conectado: " + session.getUri());

	System.out.println("Conexión WebSocket establecida para el usuario " + userId + " en la reunión " + meetingId);

	meetings.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>())
		.add(new UserSession(userId, session, UserRole.PARTICIPANT));
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    String json = mapper.writeValueAsString(
	        new NotificationMessageDto("notification", userId + " se ha unido a la reunión " + meetingId)
	    );
	    notificationWebSocketHandler.sendNotification(meetingId, json);
	} catch (JsonProcessingException e) {
	    e.printStackTrace(); 
	}

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Long meetingId = getMeetingId(session);
        ObjectMapper mapper = new ObjectMapper();
        SignalMessageDto signal = mapper.readValue(message.getPayload(), SignalMessageDto.class);

        if (isMediaAction(signal.getType())) {
            try {
                String response = null;

                switch (signal.getType()) {
                    case "get-rtp-capabilities":
                        response = mediaServerClient.getRtpCapabilities(
                        	String.valueOf(meetingId)
                        	);
                        break;
                    case "create-transport":
                        response = mediaServerClient.createTransport(
                        	String.valueOf(meetingId), 
                        	signal.getSender()
                        	);
                        break;
                    case "connect-transport":
                	  response = mediaServerClient.connectTransport(
                	    String.valueOf(meetingId),
                	    signal.getSender(),     
                	    signal.getPayload()
                	  );
                	  break;
                    case "produce":
                	    JsonNode resp = mapper.readTree(response);
                	    long newProducerId = resp.get("id").asLong();
                	    long producerPeerId = signal.getSender();

                	    ObjectNode ownReply = mapper.createObjectNode();
                	    ownReply.put("action", "produce");
                	    ownReply.set("data", resp);
                	    session.sendMessage(new TextMessage(ownReply.toString()));

                	    for (UserSession peer : meetings.get(meetingId)) {
                	        if (!peer.getUserId().equals(producerPeerId) && peer.getSession().isOpen()) {
                	            ObjectNode evt = mapper.createObjectNode();
                	            evt.put("action", "new-producer");
                	            ObjectNode data = evt.putObject("data");
                	            data.put("producerPeerId", producerPeerId);
                	            data.put("producerId", newProducerId);
                	            peer.getSession().sendMessage(new TextMessage(evt.toString()));
                	        }
                	    }
                	    break;

                    case "consume":
                	  response = mediaServerClient.consume(
                	    String.valueOf(meetingId),
                	    signal.getSender(),      
                	    signal.getPayload()
                	  );
                	  break;
                    default:
                        response = "{\"error\":\"Acción no reconocida\"}";
                        break;
                }
                System.out.print("RESPONSE: " + response);
                if (response != null) {
                    ObjectNode enriched = mapper.createObjectNode();
                    enriched.put("action", signal.getType());
                    enriched.set("data", mapper.readTree(response));

                    String finalResponse = enriched.toString();
                    System.out.println("Enviando al cliente: " + finalResponse);

                    session.sendMessage(new TextMessage(finalResponse));
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

        // Si no es media, reenvía a otros usuarios en la misma reunión
        for (UserSession userSession : meetings.getOrDefault(meetingId, new CopyOnWriteArraySet<>())) {
            if (userSession.getUserId().equals(signal.getTarget()) && userSession.getSession().isOpen()) {
                userSession.getSession().sendMessage(message);
            }
        }
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