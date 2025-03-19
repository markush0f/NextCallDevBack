package com.nextcalldev.meeting_service.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;
import com.nextcalldev.meeting_service.models.entities.MeetingParticipant;

@Component
public class MeetingMapperImpl implements IMeetingMapper {

	@Override
	public Meeting createMeetingDtoToMeeting(CreateMeetingDto createMeetingDto, Long userId) {
		Meeting meeting = Meeting.builder().title(createMeetingDto.getTitle())
				.description(createMeetingDto.getDescription()).hostUserId(userId)
				.startTime(createMeetingDto.getStartTime()).endTime(createMeetingDto.getEndTime())
				.status(createMeetingDto.getStatus()).build();

		List<MeetingParticipant> participants = createMeetingDto.getParticipantIds().stream().map(participantId -> {
			MeetingParticipant participant = new MeetingParticipant();
			participant.setParticipantUserId(participantId);
			participant.setMeeting(meeting);
			return participant;
		}).collect(Collectors.toList());

		meeting.setParticipants(participants);
		return meeting;
	}

	@Override
	public MeetingResponseDto meetingToMeetingResponseDto(Meeting meeting) {
		return MeetingResponseDto.builder().id(meeting.getId()).title(meeting.getTitle())
				.description(meeting.getDescription()).hostUserId(meeting.getHostUserId())
				.startTime(meeting.getStartTime()).endTime(meeting.getEndTime())
				.participantIds(meeting.getParticipants().stream().map(MeetingParticipant::getParticipantUserId)
						.collect(Collectors.toList()))
				.build();
	}

}
