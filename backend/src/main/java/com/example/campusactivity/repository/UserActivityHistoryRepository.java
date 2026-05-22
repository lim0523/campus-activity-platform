package com.example.campusactivity.repository;

import com.example.campusactivity.dto.UserActivityHistoryResponse;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserActivityHistoryRepository extends Repository<com.example.campusactivity.entity.SysUser, Long> {

    @Query(value = """
            select new com.example.campusactivity.dto.UserActivityHistoryResponse(
                u.userId,
                u.realName,
                u.studentNo,
                a.activityId,
                a.title,
                c.categoryName,
                r.status,
                r.registeredAt,
                ch.checkinTime,
                ch.checkinResult,
                f.rating
            )
            from ActivityRegistration r
            join r.user u
            join r.activity a
            join a.category c
            left join ActivityCheckin ch on ch.registration.registrationId = r.registrationId
            left join ActivityFeedback f on f.activity.activityId = a.activityId and f.user.userId = u.userId
            where u.userId = :userId
            order by a.startTime desc
            """)
    List<UserActivityHistoryResponse> findUserHistory(@Param("userId") Long userId);
}
