package com.sczn.wearlauncher.ruisocket;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 */
public class GaoDeUtils {
    private static SimpleDateFormat sdf = null;

    public static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }
}
