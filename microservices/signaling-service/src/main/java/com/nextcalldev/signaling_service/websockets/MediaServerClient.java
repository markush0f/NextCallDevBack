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
                    System.out.println("🟢 Conectado al media-server");
                }

                @Override
                public void onMessage(String message) {
                    // Guardar la respuesta para devolverla al cliente WebSocket
                    responseQueue.offer(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔴 Desconectado del media-server");
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("❌ Error en WebSocket media-server: " + ex.getMessage());
                }
            };

            client.connectBlocking(); // Esperar a conectar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendMessageAndWait(String message) throws InterruptedException {
        client.send(message);
        return responseQueue.poll(2, TimeUnit.SECONDS); // Espera respuesta del media-server
    }

    @PreDestroy
    public void shutdown() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
}
