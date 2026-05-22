package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long feedbackId,
        Long userId,
        String realName,
        Byte rating,
        String content,
        LocalDateTime createdAt
) {
}
