package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record CheckinRecordResponse(
        Long checkinId,
        Long registrationId,
        Long userId,
        String realName,
        String studentNo,
        String checkinResult,
        LocalDateTime checkinTime,
        String note
) {
}
