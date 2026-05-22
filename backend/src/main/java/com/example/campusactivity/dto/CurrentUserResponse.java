package com.example.campusactivity.dto;

public record CurrentUserResponse(
        Long userId,
        String username,
        String realName,
        String roleCode,
        String roleName,
        String studentNo
) {
}
