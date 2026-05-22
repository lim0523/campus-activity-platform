package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record RegistrationRecordResponse(
        Long registrationId,
        Long userId,
        String realName,
        String studentNo,
        String status,
        LocalDateTime registeredAt
) {
}
