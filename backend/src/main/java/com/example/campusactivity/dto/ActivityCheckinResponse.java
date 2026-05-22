package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record ActivityCheckinResponse(
        Long checkinId,
        Long registrationId,
        Long activityId,
        Long userId,
        String realName,
        LocalDateTime checkinTime,
        String checkinResult,
        Long operatorId,
        String note
) {
}
