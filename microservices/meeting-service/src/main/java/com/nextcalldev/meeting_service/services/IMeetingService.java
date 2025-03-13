package com.nextcalldev.meeting_service.services;

import java.util.List;

import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;

public interface IMeetingService {
	
	 public MeetingResponseDto createMeeting(CreateMeetingDto createMeetingDto, Long userId);
	 
	 public List<MeetingResponseDto> findMeetingsByUserId(Long userId);
	 
	 public MeetingResponseDto findMeetingById(Long id);

}
