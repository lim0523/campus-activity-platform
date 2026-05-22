package com.example.campusactivity.controller;

import com.example.campusactivity.common.ApiResponse;
import com.example.campusactivity.service.ForumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    private final ForumService forumService;

    public ArchiveController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/activities")
    public ApiResponse<?> listArchivedActivities() {
        return ApiResponse.ok(forumService.listArchivedActivities());
    }
}
