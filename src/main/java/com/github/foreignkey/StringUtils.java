package com.github.foreignkey;


/**
 * 字符串工具类
 *
 * @author madokast
 * @version 1.0
 */

public class StringUtils {

    /**
     * 去除字符串前后的反引号
     */
    public static String dropUnquote(String str) {
        if (str.startsWith("`") && str.endsWith("`"))
            return str.substring(1, str.length() - 1);

        return str;
    }
}
