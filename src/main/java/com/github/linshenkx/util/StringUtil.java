package com.github.linshenkx.util;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-30
 * @Description: TODO
 */

public class StringUtil {
    /**
     * 首字母转小写
     * @param s
     * @return
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }
}
