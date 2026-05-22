package com.example.campusactivity.controller;

import com.example.campusactivity.common.ApiResponse;
import com.example.campusactivity.dto.CreateForumCommentRequest;
import com.example.campusactivity.dto.CreateForumPostRequest;
import com.example.campusactivity.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/tags")
    public ApiResponse<?> listTags() {
        return ApiResponse.ok(forumService.listTags());
    }

    @GetMapping("/posts")
    public ApiResponse<?> listPosts(
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long tagId
    ) {
        return ApiResponse.ok(forumService.listPosts(section, sort, tagId));
    }

    @GetMapping("/hot")
    public ApiResponse<?> listHotPosts() {
        return ApiResponse.ok(forumService.listHotPosts());
    }

    @PostMapping("/posts")
    public ApiResponse<?> createPost(@Valid @RequestBody CreateForumPostRequest request) {
        return ApiResponse.ok("post created", forumService.createPost(request));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<?> listComments(@PathVariable Long postId) {
        return ApiResponse.ok(forumService.listComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<?> createComment(@PathVariable Long postId, @Valid @RequestBody CreateForumCommentRequest request) {
        return ApiResponse.ok("comment created", forumService.createComment(postId, request));
    }

    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<?> toggleLike(@PathVariable Long postId) {
        return ApiResponse.ok(forumService.toggleLike(postId));
    }
}
