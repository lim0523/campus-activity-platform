package com.example.campusactivity.dto;

public record LoginResponse(
        String token,
        CurrentUserResponse user
) {
}
