package com.example.campusactivity.service;

import com.example.campusactivity.auth.AuthContextHolder;
import com.example.campusactivity.auth.AuthenticatedUser;
import com.example.campusactivity.dto.ArchiveActivityResponse;
import com.example.campusactivity.dto.CreateForumCommentRequest;
import com.example.campusactivity.dto.CreateForumPostRequest;
import com.example.campusactivity.dto.ForumCommentResponse;
import com.example.campusactivity.dto.ForumLikeResponse;
import com.example.campusactivity.dto.ForumPostCardResponse;
import com.example.campusactivity.dto.ForumTagOptionResponse;
import com.example.campusactivity.entity.Activity;
import com.example.campusactivity.entity.ForumComment;
import com.example.campusactivity.entity.ForumPost;
import com.example.campusactivity.entity.ForumPostLike;
import com.example.campusactivity.entity.ForumPostTag;
import com.example.campusactivity.entity.ForumTag;
import com.example.campusactivity.entity.SysUser;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.repository.ActivityRepository;
import com.example.campusactivity.repository.ForumCommentRepository;
import com.example.campusactivity.repository.ForumPostLikeRepository;
import com.example.campusactivity.repository.ForumPostRepository;
import com.example.campusactivity.repository.ForumPostTagRepository;
import com.example.campusactivity.repository.ForumTagRepository;
import com.example.campusactivity.repository.SysUserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final ForumTagRepository forumTagRepository;
    private final ForumPostTagRepository forumPostTagRepository;
    private final ForumPostLikeRepository forumPostLikeRepository;
    private final ActivityRepository activityRepository;
    private final SysUserRepository userRepository;

    public ForumService(
            ForumPostRepository forumPostRepository,
            ForumCommentRepository forumCommentRepository,
            ForumTagRepository forumTagRepository,
            ForumPostTagRepository forumPostTagRepository,
            ForumPostLikeRepository forumPostLikeRepository,
            ActivityRepository activityRepository,
            SysUserRepository userRepository
    ) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.forumTagRepository = forumTagRepository;
        this.forumPostTagRepository = forumPostTagRepository;
        this.forumPostLikeRepository = forumPostLikeRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public List<ForumTagOptionResponse> listTags() {
        return forumTagRepository.findEnabledOptions();
    }

    public List<ForumPostCardResponse> listPosts(String section, String sort, Long tagId) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        List<ForumPost> posts = forumPostRepository.findVisiblePostsWithDetails();
        if (tagId != null) {
            Set<Long> taggedPostIds = Set.copyOf(forumPostTagRepository.findPostIdsByTagId(tagId));
            posts = posts.stream().filter(post -> taggedPostIds.contains(post.getPostId())).toList();
        }
        posts = posts.stream().filter(post -> matchesSection(post, section)).collect(Collectors.toList());

        Map<Long, List<ForumTagOptionResponse>> tagMap = buildTagMap(posts.stream().map(ForumPost::getPostId).toList());
        List<ForumPostCardResponse> responses = posts.stream()
                .map(post -> toPostCardResponse(post, currentUser.userId(), tagMap.getOrDefault(post.getPostId(), List.of())))
                .collect(Collectors.toCollection(ArrayList::new));

        if ("hot".equalsIgnoreCase(sort)) {
            responses.sort(Comparator.comparingLong(ForumPostCardResponse::hotScore).reversed()
                    .thenComparing(ForumPostCardResponse::createdAt).reversed());
        } else {
            responses.sort(Comparator.comparing(ForumPostCardResponse::createdAt).reversed());
        }
        return responses;
    }

    public List<ForumPostCardResponse> listHotPosts() {
        return listPosts("ongoing", "hot", null).stream().limit(5).toList();
    }

    public List<ForumCommentResponse> listComments(Long postId) {
        return forumCommentRepository.findByPostId(postId);
    }

    @Transactional
    public ForumPostCardResponse createPost(CreateForumPostRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        SysUser author = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));
        Activity activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new BusinessException("关联活动不存在"));

        boolean finished = "finished".equals(activity.getStatus());
        if (finished && request.tagIds() != null && !request.tagIds().isEmpty()) {
            throw new BusinessException("已结束活动的讨论不允许再添加标签");
        }

        ForumPost post = new ForumPost();
        post.setActivity(activity);
        post.setAuthor(author);
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setScope(finished ? "archive" : "ongoing");
        post.setViewCount(0);
        post.setStatus("visible");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        ForumPost saved = forumPostRepository.saveAndFlush(post);

        if (!finished && request.tagIds() != null) {
            for (Long tagId : request.tagIds()) {
                ForumTag tag = forumTagRepository.findById(tagId)
                        .orElseThrow(() -> new BusinessException("论坛标签不存在"));
                ForumPostTag relation = new ForumPostTag();
                relation.setPost(saved);
                relation.setTag(tag);
                forumPostTagRepository.save(relation);
            }
        }
        return toPostCardResponse(saved, currentUser.userId(), buildTagMap(List.of(saved.getPostId())).getOrDefault(saved.getPostId(), List.of()));
    }

    @Transactional
    public ForumCommentResponse createComment(Long postId, CreateForumCommentRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        SysUser author = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("讨论帖不存在"));

        ForumComment comment = new ForumComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());
        ForumComment saved = forumCommentRepository.saveAndFlush(comment);
        post.setUpdatedAt(LocalDateTime.now());

        return new ForumCommentResponse(
                saved.getCommentId(),
                author.getUserId(),
                author.getRealName(),
                author.getRole().getRoleCode(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public ForumLikeResponse toggleLike(Long postId) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        forumPostRepository.findById(postId).orElseThrow(() -> new BusinessException("讨论帖不存在"));
        forumPostLikeRepository.findByPostPostIdAndUserUserId(postId, currentUser.userId())
                .ifPresentOrElse(
                        forumPostLikeRepository::delete,
                        () -> {
                            ForumPostLike like = new ForumPostLike();
                            like.setPost(forumPostRepository.getReferenceById(postId));
                            like.setUser(userRepository.getReferenceById(currentUser.userId()));
                            like.setCreatedAt(LocalDateTime.now());
                            forumPostLikeRepository.save(like);
                        }
                );
        boolean liked = forumPostLikeRepository.existsByPostPostIdAndUserUserId(postId, currentUser.userId());
        return new ForumLikeResponse(postId, liked, forumPostLikeRepository.countByPostPostId(postId));
    }

    public List<ArchiveActivityResponse> listArchivedActivities() {
        return activityRepository.findArchivedActivities();
    }

    private boolean matchesSection(ForumPost post, String section) {
        if (section == null || section.isBlank() || "latest".equalsIgnoreCase(section)) {
            return true;
        }
        return switch (section) {
            case "ongoing" -> "ongoing".equals(post.getScope());
            case "archive" -> "archive".equals(post.getScope());
            default -> true;
        };
    }

    private ForumPostCardResponse toPostCardResponse(ForumPost post, Long currentUserId, List<ForumTagOptionResponse> tags) {
        long commentCount = forumCommentRepository.countByPostPostId(post.getPostId());
        long likeCount = forumPostLikeRepository.countByPostPostId(post.getPostId());
        long hotScore = commentCount * 3 + likeCount * 2 + ("ongoing".equals(post.getScope()) ? 5 : 0);
        boolean liked = forumPostLikeRepository.existsByPostPostIdAndUserUserId(post.getPostId(), currentUserId);
        return new ForumPostCardResponse(
                post.getPostId(),
                post.getActivity().getActivityId(),
                post.getActivity().getTitle(),
                post.getActivity().getStatus(),
                post.getAuthor().getUserId(),
                post.getAuthor().getRealName(),
                post.getAuthor().getRole().getRoleCode(),
                post.getTitle(),
                post.getContent(),
                post.getScope(),
                post.getCreatedAt(),
                commentCount,
                likeCount,
                hotScore,
                liked,
                tags
        );
    }

    private Map<Long, List<ForumTagOptionResponse>> buildTagMap(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<ForumTagOptionResponse>> tagMap = new HashMap<>();
        for (ForumPostTag relation : forumPostTagRepository.findByPostIds(postIds)) {
            tagMap.computeIfAbsent(relation.getPost().getPostId(), ignored -> new ArrayList<>())
                    .add(new ForumTagOptionResponse(
                            relation.getTag().getTagId(),
                            relation.getTag().getTagName(),
                            relation.getTag().getColorHex()
                    ));
        }
        return tagMap;
    }
}
