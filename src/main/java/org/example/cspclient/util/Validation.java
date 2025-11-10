package org.example.cspclient.util;

public class Validation {
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
