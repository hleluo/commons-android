package com.monsent.commons.util;

/**
 * Created by Administrator on 2017/6/27.
 */

public class StringUtils {

    /**
     * 是否为NULL或空字符串
     *
     * @param source 字符串
     * @return 是否为NULL或空字符串
     */
    public static boolean isEmpty(String source) {
        return source == null || "".equals(source);
    }

    /**
     * 是否为NULL、空格或空字符串
     *
     * @param source 字符串
     * @return 是否为NULL、空格或空字符串
     */
    public static boolean isTrimEmpty(String source) {
        return source == null || "".equals(source.trim());
    }
}
