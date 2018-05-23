package com.monsent.commons.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by Administrator on 2017/6/30.
 */

/**
 * SharedPreferences工具类
 */
public class PreferencesUtils {

    private final static String SP_FILE_NAME = "sp_data";

    /**
     * 存储值
     *
     * @param context  上下文
     * @param filename 文件名
     * @param key      键名
     * @param value    值名
     * @return 是否成功
     */
    public static boolean putValue(Context context, String filename, String key, Object value) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        return editor.commit();
    }

    /**
     * 存储值
     *
     * @param context 上下文
     * @param key     键名
     * @param value   值名
     * @return 是否成功
     */
    public static boolean putValue(Context context, String key, Object value) {
        return putValue(context, SP_FILE_NAME, key, value);
    }

    /**
     * 获取值
     *
     * @param context      上下文
     * @param filename     文件名
     * @param key          键名
     * @param defaultValue 默认值
     * @param cls          类型
     * @return 值
     */
    public static Object getValue(Context context, String filename, String key, Object defaultValue, Class<?> cls) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        if (cls.getClass().isInstance(String.class)) {
            return sp.getString(key, (String) defaultValue);
        } else if (cls.getClass().isInstance(Long.class)) {
            return sp.getLong(key, (Long) defaultValue);
        } else if (cls.getClass().isInstance(Integer.class)) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if (cls.getClass().isInstance(Float.class)) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if (cls.getClass().isInstance(Boolean.class)) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param context      上下文
     * @param key          键名
     * @param defaultValue 默认值
     * @param cls          类型
     * @return 值
     */
    public static Object getValue(Context context, String key, Object defaultValue, Class<?> cls) {
        return getValue(context, SP_FILE_NAME, key, defaultValue, cls);
    }

    /**
     * 获取所有数据
     *
     * @param context  上下文
     * @param filename 文件名
     * @return 所有值
     */
    public static Map<String, ?> getAll(Context context, String filename) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 获取所有数据
     *
     * @param context 上下文
     * @return 所有值
     */
    public static Map<String, ?> getAll(Context context) {
        return getAll(context, SP_FILE_NAME);
    }

    /**
     * 移除某个key对应的值
     *
     * @param context  上下文
     * @param filename 文件名
     * @param key      键名
     */
    public static void remove(Context context, String filename, String key) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 移除某个key对应的值
     *
     * @param context 上下文
     * @param key     键名
     */
    public static void remove(Context context, String key) {
        remove(context, SP_FILE_NAME, key);
    }

    /**
     * 清除所有内容
     *
     * @param context  上下文
     * @param filename 文件名
     */
    public static void clear(Context context, String filename) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 清除所有内容
     *
     * @param context 上下文
     */
    public static void clear(Context context) {
        clear(context, SP_FILE_NAME);
    }

}
