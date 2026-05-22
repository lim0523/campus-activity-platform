package com.example.campusactivity.repository;

import com.example.campusactivity.entity.ForumPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    @Query("""
            select distinct p from ForumPost p
            join fetch p.activity a
            join fetch p.author u
            join fetch u.role r
            where p.status = 'visible'
            """)
    List<ForumPost> findVisiblePostsWithDetails();
}
