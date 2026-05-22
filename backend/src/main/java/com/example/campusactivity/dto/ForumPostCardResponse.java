package com.example.campusactivity.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ForumPostCardResponse(
        Long postId,
        Long activityId,
        String activityTitle,
        String activityStatus,
        Long authorId,
        String authorName,
        String authorRoleCode,
        String title,
        String content,
        String scope,
        LocalDateTime createdAt,
        long commentCount,
        long likeCount,
        long hotScore,
        boolean likedByCurrentUser,
        List<ForumTagOptionResponse> tags
) {
}
