package com.real.matcher.util;

import java.util.UUID;

public class UUIDUtil {

    public static boolean isValidUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}