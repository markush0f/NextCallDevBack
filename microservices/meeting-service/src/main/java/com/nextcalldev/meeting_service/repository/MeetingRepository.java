package com.nextcalldev.meeting_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nextcalldev.meeting_service.models.entities.Meeting;
import com.nextcalldev.meeting_service.models.entities.MeetingParticipant;

public interface MeetingRepository extends CrudRepository<Meeting, Long> {

    @Query("SELECT m FROM Meeting m WHERE m.hostUserId = :hostUserId")
    List<Meeting> findByHostUserId(@Param("hostUserId") Long hostUserId);
    
//    List<MeetingParticipant> findByParticipantUserId(Long participantUserId);


}
