package com.example.campusactivity.dto;

import java.time.LocalDateTime;

public record ForumCommentResponse(
        Long commentId,
        Long authorId,
        String authorName,
        String authorRoleCode,
        String content,
        LocalDateTime createdAt
) {
}
