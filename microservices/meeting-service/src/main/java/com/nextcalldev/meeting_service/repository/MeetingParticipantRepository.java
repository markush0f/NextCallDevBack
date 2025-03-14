package com.nextcalldev.meeting_service.repository;

import java.util.List;

import com.nextcalldev.meeting_service.models.entities.MeetingParticipant;
import org.springframework.data.repository.CrudRepository;

public interface MeetingParticipantRepository extends CrudRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByParticipantUserId(Long participantUserId);
}
