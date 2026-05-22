package com.example.campusactivity.repository;

import com.example.campusactivity.dto.ActivityStatisticsResponse;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ActivityStatisticsRepository extends Repository<com.example.campusactivity.entity.Activity, Long> {

    @Query(value = """
            select new com.example.campusactivity.dto.ActivityStatisticsResponse(
                a.activityId,
                a.title,
                c.categoryName,
                o.realName,
                a.capacity,
                count(distinct case when r.status in ('approved', 'attended') then r.registrationId end),
                count(distinct ch.checkinId),
                round(
                    (count(distinct ch.checkinId) * 100.0) /
                    nullif(count(distinct case when r.status in ('approved', 'attended') then r.registrationId end), 0),
                    2
                )
            )
            from Activity a
            join a.category c
            join a.organizer o
            left join ActivityRegistration r on r.activity.activityId = a.activityId
            left join ActivityCheckin ch on ch.registration.registrationId = r.registrationId
            group by a.activityId, a.title, c.categoryName, o.realName, a.capacity
            order by a.activityId asc
            """)
    List<ActivityStatisticsResponse> findActivityStatistics();
}
