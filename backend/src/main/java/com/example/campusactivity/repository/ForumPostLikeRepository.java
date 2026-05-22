package com.example.campusactivity.repository;

import com.example.campusactivity.entity.ForumPostLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {

    long countByPostPostId(Long postId);

    boolean existsByPostPostIdAndUserUserId(Long postId, Long userId);

    Optional<ForumPostLike> findByPostPostIdAndUserUserId(Long postId, Long userId);
}
