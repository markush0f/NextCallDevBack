package com.nextcalldev.meeting_service.mappers;

import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;

public interface IMeetingMapper {

	Meeting createMeetingDtoToMeeting(CreateMeetingDto createMeetingDto, Long id);
	
	MeetingResponseDto meetingToMeetingResponseDto(Meeting meeting);
	
	
}
