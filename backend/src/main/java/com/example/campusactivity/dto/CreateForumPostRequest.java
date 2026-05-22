package com.example.campusactivity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateForumPostRequest(
        @NotNull Long activityId,
        @NotBlank String title,
        @NotBlank String content,
        List<Long> tagIds
) {
}
