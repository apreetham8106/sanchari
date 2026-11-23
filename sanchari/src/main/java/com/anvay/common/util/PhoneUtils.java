package com.anvay.common.util;

import com.anvay.common.exception.BadRequestException;

public final class PhoneUtils {
    private PhoneUtils() {
    }

    /** Normalises to E.164. A bare 10-digit number is treated as Indian (+91). */
    public static String normalize(String raw) {
        if (raw == null) {
            throw new BadRequestException("Phone number is required");
        }
        String p = raw.replaceAll("[\\s\\-()]", "");
        if (p.matches("^[6-9]\\d{9}$")) {
            p = "+91" + p;
        } else if (p.matches("^91[6-9]\\d{9}$")) {
            p = "+" + p;
        }
        if (!p.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new BadRequestException("Invalid phone number");
        }
        return p;
    }
}
