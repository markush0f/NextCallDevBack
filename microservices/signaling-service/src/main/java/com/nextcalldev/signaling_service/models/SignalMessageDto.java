package com.nextcalldev.signaling_service.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SignalMessageDto {

    private String type;
    private Long sender;
    private Long target;

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = OfferPayload.class, name = "offer"),
        @JsonSubTypes.Type(value = AnswerPayload.class, name = "answer"),
        @JsonSubTypes.Type(value = IceCandidatePayload.class, name = "ice-candidate")
    })
    private SignalPayload payload;
}