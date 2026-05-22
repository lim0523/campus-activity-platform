package com.example.campusactivity.repository;

import com.example.campusactivity.dto.ForumCommentResponse;
import com.example.campusactivity.entity.ForumComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    long countByPostPostId(Long postId);

    @Query("""
            select new com.example.campusactivity.dto.ForumCommentResponse(
                c.commentId,
                u.userId,
                u.realName,
                r.roleCode,
                c.content,
                c.createdAt
            )
            from ForumComment c
            join c.author u
            join u.role r
            where c.post.postId = :postId
            order by c.createdAt asc
            """)
    List<ForumCommentResponse> findByPostId(@Param("postId") Long postId);
}
