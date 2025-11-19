package com.example.messenger.store;

public class SessionStore {
    private static String token;
    private static Long userId;

    public static void setSession(Long id, String t) {
        userId = id;
        token = t;
    }

    public static String getToken() {
        return token;
    }

    public static Long getUserId() {
        return userId;
    }

    public static void clear() {
        token = null;
        userId = null;
    }
}
