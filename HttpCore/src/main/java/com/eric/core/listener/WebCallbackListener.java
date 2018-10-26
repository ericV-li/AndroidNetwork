package com.eric.core.listener;

import okhttp3.Headers;

/**
 * @author li
 * @Package com.eric.core.listener
 * @Title: WebCallbackListener
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 网络请求回调接口
 */
public interface WebCallbackListener<T> {
    /**
     * 网络请求回调成功方法
     *
     * @param tag      网络请求标志位
     * @param httpCode httpCode响应吗
     * @param t        返回数据泛型实体
     * @param headers  网络请求响应头
     */
    void onSuccess(Object tag, int httpCode, T t, Headers headers);

    /**
     * 网络请求失败但有响应
     *
     * @param tag      网络请求标志位
     * @param httpCode httpCode响应吗
     * @param data     响应数据
     */
    void onData(Object tag, int httpCode, String data);

    /**
     * 网络请求失败且无响应
     *
     * @param tag 网络请求标志位
     * @param e   异常信息
     */
    void onFailure(Object tag, Exception e);
}
