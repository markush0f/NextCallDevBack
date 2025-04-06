package com.nextcalldev.signaling_service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferPayload implements SignalPayload {
    private String sdp;
    private String type;
}
