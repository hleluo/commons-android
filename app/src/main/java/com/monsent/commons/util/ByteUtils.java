package com.monsent.commons.util;

import java.util.Locale;

/**
 * Created by Administrator on 2017/6/27.
 */

public class ByteUtils {

    private final static String SEPARATOR_SPACE = " ";

    /**
     * 十六进制字符串转字节数组
     *
     * @param source    十六进制字符串
     * @param separator 分隔符
     * @return 字节数组
     */
    public static byte[] getHexBytes(String source, String separator) {
        if (source == null || "".equals(source.trim())) {
            return null;
        }
        separator = separator == null ? "" : separator;
        final String replacement = "#";
        if ("".equals(separator)) {
            StringBuilder sb = new StringBuilder();
            //每两个之间用replacement隔开
            for (int i = 0; i < source.length(); i += 2) {
                int endIndex = i + 2;
                endIndex = endIndex > source.length() ? source.length() : endIndex;
                sb.append(String.format("%s%s", source.substring(i, endIndex), replacement));
            }
            source = sb.toString();
        } else {
            source = source.replaceAll(separator, replacement);
        }
        if (source.endsWith(replacement)) {
            source = source.substring(0, source.length() - 1);
        }
        String[] data = source.split(replacement);
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            String value = data[i].trim();
            value = value.length() == 1 ? "0" + value : value;
            bytes[i] = (byte) Integer.parseInt(value, 16);
        }
        return bytes;
    }

    /**
     * 十六进制字符串转字节数组
     *
     * @param source 十六进制字符串，如12 0F 3A EE
     * @return 字节数组
     */
    public static byte[] getHexBytes(String source) {
        return getHexBytes(source, SEPARATOR_SPACE);
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes     字节数组
     * @param separator 分隔符
     * @return 十六进制字符串
     */
    public static String getHexStr(byte[] bytes, String separator) {
        if (bytes == null) {
            return null;
        }
        separator = separator == null ? SEPARATOR_SPACE : separator;
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            hex = hex.length() == 1 ? "0" + hex : hex;
            sb.append(String.format("%s%s", hex.toUpperCase(Locale.getDefault()), separator));
        }
        String data = sb.toString().trim();
        if (data.endsWith(separator)) {
            data = data.substring(0, data.length() - 1);
        }
        return data;
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串， 如12 0F 3A EE
     */
    public static String getHexStr(byte[] bytes) {
        return getHexStr(bytes, SEPARATOR_SPACE);
    }

}
