package com.nextcalldev.signaling_service.websockets;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Component
public class MediaServerClient {

    private WebSocketClient client;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        try {
            client = new WebSocketClient(new URI("ws://localhost:3001")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Conectado al media-server");
                }

                @Override
                public void onMessage(String message) {
                    responseQueue.offer(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Desconectado del media-server");
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println(" Error en WebSocket media-server: " + ex.getMessage());
                }
            };

            client.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendMessageAndWait(String message) throws InterruptedException {
	    for (int i = 0; i < 10; i++) {
	        if (client.isOpen()) break;
	        Thread.sleep(200);
	    }

	    if (!client.isOpen()) {
	        System.out.println("❌ No se pudo conectar al media-server WebSocket.");
	        return "{\"error\":\"WebSocket no disponible\"}";
	    }

	    client.send(message);
	    return responseQueue.poll(5, TimeUnit.SECONDS);
	}


    @PreDestroy
    public void shutdown() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
}
