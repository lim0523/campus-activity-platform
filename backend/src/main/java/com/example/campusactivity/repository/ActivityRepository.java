package com.example.campusactivity.repository;

import com.example.campusactivity.dto.ActivityCardResponse;
import com.example.campusactivity.dto.ActivityDetailResponse;
import com.example.campusactivity.dto.ArchiveActivityResponse;
import com.example.campusactivity.entity.Activity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    long countByStatus(String status);

    @Query("""
            select new com.example.campusactivity.dto.ActivityCardResponse(
                a.activityId,
                a.title,
                c.categoryName,
                o.realName,
                a.location,
                a.startTime,
                a.capacity,
                count(r.registrationId),
                a.status
            )
            from Activity a
            join a.category c
            join a.organizer o
            left join ActivityRegistration r
                on r.activity.activityId = a.activityId
               and r.status in ('approved', 'attended')
            group by a.activityId, a.title, c.categoryName, o.realName, a.location, a.startTime, a.capacity, a.status
            order by a.startTime asc
            """)
    List<ActivityCardResponse> findActivityCards();

    @Query("""
            select new com.example.campusactivity.dto.ActivityDetailResponse(
                a.activityId,
                c.categoryId,
                c.categoryName,
                o.userId,
                o.realName,
                a.title,
                a.description,
                a.location,
                a.startTime,
                a.endTime,
                a.registrationDeadline,
                a.capacity,
                a.status
            )
            from Activity a
            join a.category c
            join a.organizer o
            where a.activityId = :activityId
            """)
    Optional<ActivityDetailResponse> findDetailByActivityId(@Param("activityId") Long activityId);

    @Query("""
            select new com.example.campusactivity.dto.ArchiveActivityResponse(
                a.activityId,
                a.title,
                c.categoryName,
                o.realName,
                a.endTime,
                round(avg(f.rating * 1.0), 2),
                count(f.feedbackId)
            )
            from Activity a
            join a.category c
            join a.organizer o
            left join ActivityFeedback f on f.activity.activityId = a.activityId
            where a.status = 'finished'
            group by a.activityId, a.title, c.categoryName, o.realName, a.endTime
            order by a.endTime desc
            """)
    List<ArchiveActivityResponse> findArchivedActivities();
}
