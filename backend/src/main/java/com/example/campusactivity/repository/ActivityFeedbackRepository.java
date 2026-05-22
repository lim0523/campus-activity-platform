package com.example.campusactivity.repository;

import com.example.campusactivity.dto.FeedbackResponse;
import com.example.campusactivity.entity.ActivityFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityFeedbackRepository extends JpaRepository<ActivityFeedback, Long> {

    boolean existsByActivityActivityIdAndUserUserId(Long activityId, Long userId);

    @Query("select avg(f.rating * 1.0) from ActivityFeedback f")
    Double findAverageRating();

    @Query("""
            select new com.example.campusactivity.dto.FeedbackResponse(
                f.feedbackId,
                u.userId,
                u.realName,
                f.rating,
                f.content,
                f.createdAt
            )
            from ActivityFeedback f
            join f.user u
            where f.activity.activityId = :activityId
            order by f.createdAt desc
            """)
    List<FeedbackResponse> findByActivityId(@Param("activityId") Long activityId);
}
