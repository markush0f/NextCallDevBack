package com.nextcalldev.meeting_service.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nextcalldev.meeting_service.mappers.IMeetingMapper;
import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;
import com.nextcalldev.meeting_service.repository.MeetingRepository;

@Service
public class MeetingServiceImpl implements IMeetingService {

	private final MeetingRepository meetingRepository;
	private final IMeetingMapper meetingMapper;

	public MeetingServiceImpl(MeetingRepository meetingRepository,
			IMeetingMapper meetingMapper) {
		this.meetingRepository = meetingRepository;
		this.meetingMapper = meetingMapper;
	}

	@Override
	public MeetingResponseDto createMeeting(CreateMeetingDto createMeetingDto, Long userId) {
		Meeting meeting = meetingRepository.save(
				meetingMapper.createMeetingDtoToMeeting(createMeetingDto, userId));
		return meetingMapper.meetingToMeetingResponseDto(meeting);
	}

	@Override
	public List<MeetingResponseDto> findMeetingsByUserId(Long userId) {
		List<Meeting> meetings = meetingRepository
				.findByParticipantUserId(userId);
		return meetings.stream().map(meetingMapper::meetingToMeetingResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	public MeetingResponseDto findMeetingById(Long id) {
		Meeting meeting = meetingRepository.findById(id).orElseThrow();
		return meetingMapper.meetingToMeetingResponseDto(meeting);
	}

}
