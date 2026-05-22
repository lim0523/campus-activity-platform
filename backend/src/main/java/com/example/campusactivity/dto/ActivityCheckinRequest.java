package com.example.campusactivity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivityCheckinRequest(
        @NotNull Long userId,
        @NotBlank String checkinResult,
        String note
) {
}
