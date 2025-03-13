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
@Builder
@Getter
@Setter
public class CreateMeetingDto {

	private String title;
	
	private String description;
	
	private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private MeetingStatus status; 
    
    private List<String> participantIds;	
}
