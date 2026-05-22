package com.example.campusactivity.repository;

import com.example.campusactivity.dto.RegistrationRecordResponse;
import com.example.campusactivity.entity.ActivityRegistration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

    boolean existsByActivityActivityIdAndUserUserId(Long activityId, Long userId);

    long countByActivityActivityIdAndStatusIn(Long activityId, java.util.Collection<String> statuses);

    long countByStatusIn(java.util.Collection<String> statuses);

    java.util.Optional<ActivityRegistration> findByActivityActivityIdAndUserUserId(Long activityId, Long userId);

    @Query("""
            select new com.example.campusactivity.dto.RegistrationRecordResponse(
                r.registrationId,
                u.userId,
                u.realName,
                u.studentNo,
                r.status,
                r.registeredAt
            )
            from ActivityRegistration r
            join r.user u
            where r.activity.activityId = :activityId
            order by r.registeredAt asc
            """)
    List<RegistrationRecordResponse> findByActivityId(@Param("activityId") Long activityId);
}
