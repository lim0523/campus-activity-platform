package com.example.campusactivity.repository;

import com.example.campusactivity.dto.UserOptionResponse;
import com.example.campusactivity.entity.SysUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsernameAndStatus(String username, String status);

    @Query("""
            select new com.example.campusactivity.dto.UserOptionResponse(
                u.userId,
                u.realName,
                r.roleCode
            )
            from SysUser u
            join u.role r
            where r.roleCode in ('organizer', 'admin')
              and u.status = 'enabled'
            order by u.realName asc
            """)
    List<UserOptionResponse> findOrganizerOptions();

    @Query("""
            select new com.example.campusactivity.dto.UserOptionResponse(
                u.userId,
                u.realName,
                r.roleCode
            )
            from SysUser u
            join u.role r
            where u.status = 'enabled'
            order by u.realName asc
            """)
    List<UserOptionResponse> findEnabledUsers();
}
