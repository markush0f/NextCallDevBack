package com.nextcalldev.signaling_service.models;

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
    private Object payload;
}