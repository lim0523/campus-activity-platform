package com.example.campusactivity.repository;

import com.example.campusactivity.dto.ForumTagOptionResponse;
import com.example.campusactivity.entity.ForumTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ForumTagRepository extends JpaRepository<ForumTag, Long> {

    @Query("""
            select new com.example.campusactivity.dto.ForumTagOptionResponse(
                t.tagId,
                t.tagName,
                t.colorHex
            )
            from ForumTag t
            where t.status = 'enabled'
            order by t.tagName asc
            """)
    List<ForumTagOptionResponse> findEnabledOptions();
}
