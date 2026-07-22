package com.campus.client.util;

import java.util.prefs.Preferences;

public class AuthStorage {
    private static final Preferences PREFS = Preferences.userNodeForPackage(AuthStorage.class);
    private static final String TOKEN_KEY = "campus_token";
    private static final String ROLE_KEY = "campus_role";
    private static final String USERNAME_KEY = "campus_username";
    private static final String USER_ID_KEY = "campus_user_id";

    public static void save(String token, String role, String username, int userId) {
        PREFS.put(TOKEN_KEY, token != null ? token : "");
        PREFS.put(ROLE_KEY, role != null ? role : "");
        PREFS.put(USERNAME_KEY, username != null ? username : "");
        PREFS.putInt(USER_ID_KEY, userId);
    }

    public static String getToken() { return PREFS.get(TOKEN_KEY, null); }
    public static String getRole() { return PREFS.get(ROLE_KEY, null); }
    public static String getUsername() { return PREFS.get(USERNAME_KEY, null); }
    public static int getUserId() { return PREFS.getInt(USER_ID_KEY, 0); }

    public static void clear() {
        PREFS.remove(TOKEN_KEY);
        PREFS.remove(ROLE_KEY);
        PREFS.remove(USERNAME_KEY);
        PREFS.remove(USER_ID_KEY);
    }

    public static boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }
}
