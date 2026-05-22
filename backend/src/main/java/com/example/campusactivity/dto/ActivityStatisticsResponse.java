package com.example.campusactivity.dto;

public record ActivityStatisticsResponse(
        Long activityId,
        String title,
        String categoryName,
        String organizerName,
        Integer capacity,
        Long registeredCount,
        Long checkinCount,
        Double checkinRatePercent
) {
}
