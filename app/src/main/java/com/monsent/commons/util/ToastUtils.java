package com.monsent.commons.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/7/7.
 */

public class ToastUtils {

    /**
     * 显示Toast
     * @param context 上下文
     * @param message 消息
     */
    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
