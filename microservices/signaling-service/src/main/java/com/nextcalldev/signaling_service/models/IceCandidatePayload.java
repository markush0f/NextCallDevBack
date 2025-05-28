package com.nextcalldev.signaling_service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IceCandidatePayload implements SignalPayload {
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
}

