package com.nextcalldev.meeting_service.models.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.nextcalldev.meeting_service.common.MeetingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MeetingResponseDto {

    private Long id;

    private String title;

    private String description;

    private Long hostUserId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private MeetingStatus status;

    private List<String> participantIds;
}
