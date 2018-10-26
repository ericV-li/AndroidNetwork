package com.eric.http.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author li
 * @Package com.eric.http.utils
 * @Title: CompatErrorUtil
 * @Description: Copyright (c)
 * Create DateTime: 2017/11/29
 */
public class CompatErrorUtil {
    /**
     * 服务端返回的数据不符合，需要客户端做统一处理
     *
     * @param data
     * @return
     */
    public static String replaceJson(String data, boolean isDebug) {
        if (isDebug) {
            Log.e("response body", TextUtils.isEmpty(data) ? "服务端返回null或者空串" : data);
        }
        return data;
    }
}
