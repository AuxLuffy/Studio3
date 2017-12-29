package com.example.sunzh.studio3.Utils;

import android.text.TextUtils;

/**
 * Created by sunzh on 2017/12/21.
 *
 * @author sunzh
 */

class StringUtils {
    public static boolean isBlank(String timestampString) {
        if (timestampString == null || TextUtils.isEmpty(timestampString.trim())) {
            return true;
        }
        return false;
    }
}
