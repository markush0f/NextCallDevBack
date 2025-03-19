package com.nextcalldev.meeting_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextcalldev.meeting_service.models.dto.CreateMeetingDto;
import com.nextcalldev.meeting_service.services.IMeetingService;
import com.nextcalldev.meeting_service.services.MeetingServiceImpl;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController()
@RequestMapping("/meeting")
public class MeetingController {

	private final IMeetingService meetingServiceImpl;

	public MeetingController(MeetingServiceImpl meetingServiceImpl) {
		this.meetingServiceImpl = meetingServiceImpl;
	}

	@PostMapping("{userId}")
	public ResponseEntity<?> createMeeting(
			@RequestBody() CreateMeetingDto createMeetingDto,
			@PathVariable() Long userId) {
		return ResponseEntity
				.ok(meetingServiceImpl.createMeeting(createMeetingDto, userId));
	}

	@GetMapping("{id}")
	public ResponseEntity<?> findMeetingById(@PathVariable() Long id) {
		return ResponseEntity.ok(meetingServiceImpl.findMeetingById(id));
	}

	@GetMapping("host/{hostId}")
	public ResponseEntity<?> findMeetingsByHostUserId(@PathVariable() Long hostId) {
		System.out.println("El User id: " + hostId);
		return ResponseEntity
				.ok(meetingServiceImpl.findMeetingsByHostUserId(hostId));
	}
	
	@GetMapping("participant/{userId}")
	public ResponseEntity<?> findMeetingsbyUserId(@PathVariable() Long userId){
		return ResponseEntity
				.ok(meetingServiceImpl.findMeetingsByUserId(userId));
	}

	@GetMapping("check")
	public String check() {
		return "Meeting fixing";
	}

}
