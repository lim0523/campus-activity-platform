package com.example.campusactivity.dto;

public record DashboardSummaryResponse(
        long totalActivities,
        long publishedActivities,
        long finishedActivities,
        long totalRegistrations,
        long totalCheckins,
        Double averageFeedbackRating
) {
}
