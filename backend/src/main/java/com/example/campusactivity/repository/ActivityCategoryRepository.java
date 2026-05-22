package com.example.campusactivity.repository;

import com.example.campusactivity.dto.CategoryOptionResponse;
import com.example.campusactivity.entity.ActivityCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {

    @Query("""
            select new com.example.campusactivity.dto.CategoryOptionResponse(
                c.categoryId,
                c.categoryName
            )
            from ActivityCategory c
            where c.status = 'enabled'
            order by c.categoryName asc
            """)
    List<CategoryOptionResponse> findEnabledOptions();
}
