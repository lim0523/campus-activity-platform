package com.example.campusactivity.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateForumCommentRequest(
        @NotBlank String content
) {
}
