package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record ArchiveActivityResponse(
        Long activityId,
        String title,
        String categoryName,
        String organizerName,
        LocalDateTime endTime,
        Double averageRating,
        Long feedbackCount
) {
}
