package com.nextcalldev.meeting_service.mappers;

import org.springframework.stereotype.Component;

import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;

@Component
public class MeetingMapperImpl implements IMeetingMapper {

	@Override
	public Meeting createMeetingDtoToMeeting(CreateMeetingDto createMeetingDto, Long userId) {
		return Meeting.builder()
				.title(createMeetingDto.getTitle())
				.description(createMeetingDto.getDescription())
				.hostUserId(userId)
				.startTime(createMeetingDto.getStartTime())
				.endTime(createMeetingDto.getEndTime())
				.status(createMeetingDto.getStatus())
				.build();
	}

	@Override
	public MeetingResponseDto meetingToMeetingResponseDto(Meeting meeting) {
		return MeetingResponseDto.builder()
				.id(meeting.getId())
				.title(meeting.getTitle())
				.description(meeting.getDescription())
				.hostUserId(meeting.getHostUserId())
				.startTime(meeting.getStartTime())
				.endTime(meeting.getEndTime())
				.build();
	}

}
