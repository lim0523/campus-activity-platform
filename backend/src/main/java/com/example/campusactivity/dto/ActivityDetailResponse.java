package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record ActivityDetailResponse(
        Long activityId,
        Long categoryId,
        String categoryName,
        Long organizerId,
        String organizerName,
        String title,
        String description,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime registrationDeadline,
        Integer capacity,
        String status
) {
}
