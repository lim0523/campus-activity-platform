package com.example.campusactivity.controller;

import com.example.campusactivity.common.ApiResponse;
import com.example.campusactivity.dto.ActivityCheckinRequest;
import com.example.campusactivity.dto.CreateFeedbackRequest;
import com.example.campusactivity.dto.CreateActivityRequest;
import com.example.campusactivity.dto.RegisterActivityRequest;
import com.example.campusactivity.dto.UpdateActivityRequest;
import com.example.campusactivity.service.ActivityCommandService;
import com.example.campusactivity.service.ActivityQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityQueryService activityQueryService;
    private final ActivityCommandService activityCommandService;

    public ActivityController(ActivityQueryService activityQueryService, ActivityCommandService activityCommandService) {
        this.activityQueryService = activityQueryService;
        this.activityCommandService = activityCommandService;
    }

    @GetMapping
    public ApiResponse<?> listActivities() {
        return ApiResponse.ok(activityQueryService.listActivities());
    }

    @GetMapping("/{activityId}")
    public ApiResponse<?> getActivityDetail(@PathVariable Long activityId) {
        return ApiResponse.ok(activityQueryService.getActivityDetail(activityId));
    }

    @GetMapping("/{activityId}/registrations")
    public ApiResponse<?> listActivityRegistrations(@PathVariable Long activityId) {
        return ApiResponse.ok(activityQueryService.listRegistrations(activityId));
    }

    @GetMapping("/{activityId}/checkins")
    public ApiResponse<?> listActivityCheckins(@PathVariable Long activityId) {
        return ApiResponse.ok(activityQueryService.listCheckins(activityId));
    }

    @GetMapping("/{activityId}/feedbacks")
    public ApiResponse<?> listActivityFeedbacks(@PathVariable Long activityId) {
        return ApiResponse.ok(activityQueryService.listFeedbacks(activityId));
    }

    @GetMapping("/statistics")
    public ApiResponse<?> listStatistics() {
        return ApiResponse.ok(activityQueryService.listStatistics());
    }

    @GetMapping("/dashboard-summary")
    public ApiResponse<?> getDashboardSummary() {
        return ApiResponse.ok(activityQueryService.getDashboardSummary());
    }

    @PostMapping
    public ApiResponse<?> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return ApiResponse.ok("activity created", activityCommandService.createActivity(request));
    }

    @PutMapping("/{activityId}")
    public ApiResponse<?> updateActivity(@PathVariable Long activityId, @Valid @RequestBody UpdateActivityRequest request) {
        return ApiResponse.ok("activity updated", activityCommandService.updateActivity(activityId, request));
    }

    @PostMapping("/{activityId}/registrations")
    public ApiResponse<?> registerActivity(@PathVariable Long activityId, @Valid @RequestBody RegisterActivityRequest request) {
        return ApiResponse.ok("registration created", activityCommandService.registerActivity(activityId, request));
    }

    @PostMapping("/{activityId}/checkins")
    public ApiResponse<?> checkIn(@PathVariable Long activityId, @Valid @RequestBody ActivityCheckinRequest request) {
        return ApiResponse.ok("checkin created", activityCommandService.checkIn(activityId, request));
    }

    @PostMapping("/{activityId}/feedbacks")
    public ApiResponse<?> createFeedback(@PathVariable Long activityId, @Valid @RequestBody CreateFeedbackRequest request) {
        return ApiResponse.ok("feedback created", activityCommandService.createFeedback(activityId, request));
    }
}
