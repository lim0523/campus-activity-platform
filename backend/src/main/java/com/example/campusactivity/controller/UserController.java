package com.example.campusactivity.controller;

import com.example.campusactivity.common.ApiResponse;
import com.example.campusactivity.service.ActivityQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ActivityQueryService activityQueryService;

    public UserController(ActivityQueryService activityQueryService) {
        this.activityQueryService = activityQueryService;
    }

    @GetMapping("/{userId}/activities")
    public ApiResponse<?> listUserActivityHistory(@PathVariable Long userId) {
        return ApiResponse.ok(activityQueryService.listUserHistory(userId));
    }

    @GetMapping
    public ApiResponse<?> listEnabledUsers() {
        return ApiResponse.ok(activityQueryService.listEnabledUsers());
    }

    @GetMapping("/organizers")
    public ApiResponse<?> listOrganizerOptions() {
        return ApiResponse.ok(activityQueryService.listOrganizerOptions());
    }
}
