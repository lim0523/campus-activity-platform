package com.example.campusactivity.dto;

public record UserOptionResponse(
        Long userId,
        String realName,
        String roleCode
) {
}
