package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record UserActivityHistoryResponse(
        Long userId,
        String realName,
        String studentNo,
        Long activityId,
        String title,
        String categoryName,
        String registrationStatus,
        LocalDateTime registeredAt,
        LocalDateTime checkinTime,
        String checkinResult,
        Byte rating
) {
}
