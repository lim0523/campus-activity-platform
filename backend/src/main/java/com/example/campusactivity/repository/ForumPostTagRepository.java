package com.example.campusactivity.repository;

import com.example.campusactivity.entity.ForumPostTag;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumPostTagRepository extends JpaRepository<ForumPostTag, Long> {

    @Query("""
            select pt from ForumPostTag pt
            join fetch pt.tag t
            join fetch pt.post p
            where p.postId in :postIds
            """)
    List<ForumPostTag> findByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("""
            select pt.post.postId from ForumPostTag pt
            where pt.tag.tagId = :tagId
            """)
    List<Long> findPostIdsByTagId(@Param("tagId") Long tagId);

    void deleteByPostPostId(Long postId);
}
