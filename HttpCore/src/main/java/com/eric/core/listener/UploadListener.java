package com.eric.core.listener;

import okhttp3.Headers;

/**
 * @author li
 * @Package com.eric.core.listener
 * @Title: UploadListener
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 上传监听回调接口
 */
public interface UploadListener<T> {

    /**
     * 上传成功回调方法
     *
     * @param tag      网络请求标志位
     * @param httpCode http响应码 200-300
     * @param result   响应结果实体
     * @param headers  响应头
     */
    void onSuccess(Object tag, int httpCode, T result, Headers headers);

    /**
     * 上传失败但有响应
     *
     * @param tag      网络请求标志位
     * @param httpCode http响应码 200-300
     * @param data     响应结果数据
     */
    void onData(Object tag, int httpCode, String data);

    /**
     * 上传失败且有响应
     *
     * @param tag 网络请求标志位
     * @param e   异常信息
     */
    void onFailure(Object tag, Exception e);

    /**
     * 上传进度回调
     *
     * @param bytesWrite 已上传字节数
     * @param totalBytes 总字节数
     * @param isDone     是否上传完成
     */
    void onProgress(long bytesWrite, long totalBytes, boolean isDone);
}
