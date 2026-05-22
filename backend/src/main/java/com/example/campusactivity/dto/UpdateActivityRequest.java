package com.example.campusactivity.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateActivityRequest(
        @NotNull Long categoryId,
        @NotBlank String title,
        String description,
        @NotBlank String location,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        @NotNull @Future LocalDateTime registrationDeadline,
        @NotNull @Min(1) Integer capacity,
        @NotBlank String status
) {
}
