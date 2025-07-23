package com.example.backend.utils;

import org.springframework.stereotype.Component;

@Component
public class RequestContext {
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static void clear() {
        currentUserId.remove();
    }
}
