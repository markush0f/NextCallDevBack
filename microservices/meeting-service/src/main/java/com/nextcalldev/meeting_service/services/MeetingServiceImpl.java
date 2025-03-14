package com.nextcalldev.meeting_service.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nextcalldev.meeting_service.client.UserServiceClient;
import com.nextcalldev.meeting_service.mappers.IMeetingMapper;
import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.models.dto.MeetingResponseDto;
import com.nextcalldev.meeting_service.models.dto.UserResponseDto;
import com.nextcalldev.meeting_service.models.entities.Meeting;
import com.nextcalldev.meeting_service.repository.MeetingRepository;

@Service
public class MeetingServiceImpl implements IMeetingService {

	private final MeetingRepository meetingRepository;
	private final IMeetingMapper meetingMapper;
	private final UserServiceClient userServiceClient;

	public MeetingServiceImpl(MeetingRepository meetingRepository,
			IMeetingMapper meetingMapper, UserServiceClient userServiceClient) {
		this.meetingRepository = meetingRepository;
		this.meetingMapper = meetingMapper;
		this.userServiceClient = userServiceClient;
	}

	@Override
	public MeetingResponseDto createMeeting(CreateMeetingDto createMeetingDto,
			Long userId) {

		Meeting meeting = meetingRepository.save(meetingMapper
				.createMeetingDtoToMeeting(createMeetingDto, userId));
		return meetingMapper.meetingToMeetingResponseDto(meeting);
	}

	@Override
	public List<MeetingResponseDto> findMeetingsByHostUserId(Long hostId) {
		UserResponseDto user = userServiceClient.getUserById(hostId);
		System.out.println("User: " + user);
		List<Meeting> meetings = meetingRepository
				.findByHostUserId(hostId);
		return meetings.stream().map(meetingMapper::meetingToMeetingResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	public MeetingResponseDto findMeetingById(Long id) {
		Meeting meeting = meetingRepository.findById(id).orElseThrow();
		return meetingMapper.meetingToMeetingResponseDto(meeting);
	}

}
