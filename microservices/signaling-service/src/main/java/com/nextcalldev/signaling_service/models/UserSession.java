package com.nextcalldev.signaling_service.models;

import org.springframework.web.socket.WebSocketSession;

import com.nextcalldev.signaling_service.common.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class UserSession {

    private Long userId;

    private WebSocketSession session;

    private UserRole userRole;
}
