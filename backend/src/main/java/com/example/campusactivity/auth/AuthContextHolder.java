package com.example.campusactivity.auth;

import com.example.campusactivity.exception.UnauthorizedException;

public final class AuthContextHolder {

    private static final ThreadLocal<AuthenticatedUser> CONTEXT = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthenticatedUser user) {
        CONTEXT.set(user);
    }

    public static AuthenticatedUser get() {
        return CONTEXT.get();
    }

    public static AuthenticatedUser require() {
        AuthenticatedUser user = CONTEXT.get();
        if (user == null) {
            throw new UnauthorizedException("请先登录");
        }
        return user;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
