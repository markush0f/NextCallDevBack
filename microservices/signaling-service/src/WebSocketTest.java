import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketTest {
    public static void main(String[] args) {
        try {
            WebSocketClient client = new WebSocketClient(new URI("ws://localhost:3001")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("✅ Conectado al media-server desde prueba");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("📩 Mensaje recibido: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔌 Conexión cerrada: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("❌ Error en WebSocket: " + ex.getMessage());
                }
            };

            client.connectBlocking();
            client.send("{\"action\":\"ping\",\"roomId\":\"test\",\"payload\":{}}");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

