package com.example.campusactivity.auth;

public record AuthenticatedUser(
        Long userId,
        String username,
        String realName,
        String roleCode
) {
    public boolean isAdmin() {
        return "admin".equals(roleCode);
    }

    public boolean canPublishActivities() {
        return "organizer".equals(roleCode) || "admin".equals(roleCode);
    }
}
