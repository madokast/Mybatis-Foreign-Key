package com.github.foreignkey.utils;


import java.util.List;

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
        if (str.startsWith("`") && str.endsWith("`")) {
            str = str.substring(1, str.length() - 1);
        }

        return str;
    }

    public static String addUnquote(String str) {
        return "`" + str + "`";
    }

    /**
     * 函数返回 stringList 中 [0,indexExcluding) 范围内问号的数目
     * 这个函数用来查看 parameterMappings 是对应第几个 ?
     *
     * @param indexExcluding index
     * @param stringList     list
     * @return stringList 中 [0,indexExcluding) 范围内问号的数目
     */
    public static int numberOfQuestionMarkBefore(int indexExcluding, List<String> stringList) {
        int count = 0;
        for (int i = 0; i < indexExcluding; i++) {
            if (stringList.get(i).equals("?")) {
                count++;
            }
        }

        return count;
    }
}
