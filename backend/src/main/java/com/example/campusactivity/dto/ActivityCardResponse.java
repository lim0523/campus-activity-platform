package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record ActivityCardResponse(
        Long activityId,
        String title,
        String categoryName,
        String organizerName,
        String location,
        LocalDateTime startTime,
        Integer capacity,
        Long approvedCount,
        String status
) {
}
