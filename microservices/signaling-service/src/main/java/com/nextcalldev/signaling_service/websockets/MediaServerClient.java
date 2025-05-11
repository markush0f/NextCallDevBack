package com.nextcalldev.signaling_service.websockets;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Component
public class MediaServerClient {

  private final WebClient webClient = WebClient.create("http://localhost:3001");

  public String getRtpCapabilities(String roomId) {
    return webClient.post()
      .uri("/get-rtp-capabilities")
      .bodyValue(Map.of("roomId", roomId))
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }

  public String createTransport(String roomId, Long senderId) {
    return webClient.post()
      .uri("/create-transport")
      .bodyValue(Map.of("roomId", roomId, "senderId", senderId))
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }

  public String connectTransport(String roomId, Long senderId, Object payload) {
    return webClient.post()
      .uri("/connect-transport")
      .bodyValue(Map.of(
        "roomId",     roomId,
        "senderId",   senderId,
        "payload",    payload
      ))
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }

  public String produce(String roomId, Long senderId, Object payload) {
    return webClient.post()
      .uri("/produce")
      .bodyValue(Map.of(
        "roomId",    roomId,
        "senderId",  senderId,
        "payload",   payload
      ))
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }

  // ← Ajustado: ahora recibe senderId
  public String consume(String roomId, Long senderId, Object payload) {
    return webClient.post()
      .uri("/consume")
      .bodyValue(Map.of(
        "roomId",     roomId,
        "senderId",   senderId,
        "payload",    payload
      ))
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }
}
