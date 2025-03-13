package com.nextcalldev.meeting_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nextcalldev.meeting_service.models.entities.Meeting;

public interface MeetingRepository extends CrudRepository<Meeting, Long>{
    
    @Query("SELECT m FROM Meeting m WHERE :userId MEMBER OF m.participantIds")
    List<Meeting> findByParticipantUserId(@Param("userId") Long userId);

}
