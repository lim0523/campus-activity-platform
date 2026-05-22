package com.example.campusactivity.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterActivityRequest(
        String remark
) {
}
