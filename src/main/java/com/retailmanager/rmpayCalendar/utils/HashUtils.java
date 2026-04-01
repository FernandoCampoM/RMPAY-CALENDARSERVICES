package com.retailmanager.rmpayCalendar.utils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.util.DigestUtils;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;

public final class HashUtils {

    private HashUtils() {
    } // evita instanciación

    public static String generateProductHash(PosProduct p) {
        String data = Objects.requireNonNull(buildProductString(p));

        return md5(data);
    }

    private static String buildProductString(PosProduct p) {
        return String.join("|",
                safe(p.getProductCode()),
                safe(p.getProductName()),
                formatNumber(p.getPrice()),
                formatNumber(p.getCurrentStock()),
                safe(p.getCategory()),
                safe(p.getDepartment()));
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String formatNumber(Number value) {
        if (value == null)
            return "0";
        return String.valueOf(value);
    }

    @SuppressWarnings("null")
    public static String md5(String data) {

        return DigestUtils.md5DigestAsHex(data.getBytes(StandardCharsets.UTF_8));
    }
}