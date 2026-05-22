package com.example.campusactivity.repository;

import com.example.campusactivity.dto.CheckinRecordResponse;
import com.example.campusactivity.entity.ActivityCheckin;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityCheckinRepository extends JpaRepository<ActivityCheckin, Long> {

    boolean existsByRegistrationRegistrationId(Long registrationId);

    long count();

    Optional<ActivityCheckin> findByRegistrationRegistrationId(Long registrationId);

    @Query("""
            select new com.example.campusactivity.dto.CheckinRecordResponse(
                c.checkinId,
                r.registrationId,
                u.userId,
                u.realName,
                u.studentNo,
                c.checkinResult,
                c.checkinTime,
                c.note
            )
            from ActivityCheckin c
            join c.registration r
            join r.user u
            where r.activity.activityId = :activityId
            order by c.checkinTime desc
            """)
    List<CheckinRecordResponse> findByActivityId(@Param("activityId") Long activityId);
}
