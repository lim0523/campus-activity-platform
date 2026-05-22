package com.example.campusactivity.service;

import com.example.campusactivity.auth.AuthContextHolder;
import com.example.campusactivity.auth.AuthenticatedUser;
import com.example.campusactivity.auth.JwtService;
import com.example.campusactivity.dto.CurrentUserResponse;
import com.example.campusactivity.dto.LoginRequest;
import com.example.campusactivity.dto.LoginResponse;
import com.example.campusactivity.entity.SysUser;
import com.example.campusactivity.exception.UnauthorizedException;
import com.example.campusactivity.repository.SysUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(SysUserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsernameAndStatus(request.username(), "enabled")
                .orElseThrow(() -> new UnauthorizedException("账号或密码错误"));
        if (!user.getPasswordHash().equals(request.password())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getUserId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole().getRoleCode()
        );
        return new LoginResponse(jwtService.createToken(authenticatedUser), toCurrentUser(user));
    }

    @Transactional
    public CurrentUserResponse currentUser() {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        SysUser user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new UnauthorizedException("当前登录用户不存在"));
        return toCurrentUser(user);
    }

    private CurrentUserResponse toCurrentUser(SysUser user) {
        return new CurrentUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole().getRoleCode(),
                user.getRole().getRoleName(),
                user.getStudentNo()
        );
    }
}
