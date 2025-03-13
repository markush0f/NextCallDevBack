package com.nextcalldev.meeting_service.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.nextcalldev.meeting_service.common.MeetingStatus;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private Long hostUserId; // ID del usuario creador

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String meetingUrl;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;

    @ElementCollection
    private List<String> participantIds; // IDs de los usuarios invitados
}
