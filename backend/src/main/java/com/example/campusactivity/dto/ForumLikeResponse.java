package com.example.campusactivity.dto;

public record ForumLikeResponse(
        Long postId,
        boolean liked,
        long likeCount
) {
}
