package com.eric.core.listener;

import java.io.File;

/**
 * @author li
 * @Package com.eric.core.listener
 * @Title: DownLoadListener
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 下载文件监听接口
 */
public interface DownLoadListener {

    /**
     * 下载成功回调方法
     *
     * @param tag      网络请求标志位
     * @param httpCode http响应码 200-300
     * @param file     下载成功的文件
     */
    void onSuccess(Object tag, int httpCode, File file);

    /**
     * 下载失败但网络有响应
     *
     * @param tag      网络请求标志位
     * @param httpCode http响应码
     * @param data     返回数据
     */
    void onData(Object tag, int httpCode, String data);

    /**
     * 下载失败且网络有响应
     *
     * @param tag 网络请求标志位
     * @param e   异常信息
     */
    void onFailure(Object tag, Exception e);

    /**
     * 进度回调
     *
     * @param bytesWrite 已经下载字节数
     * @param totalBytes 总字节数
     * @param isDone     是否下载完成
     */
    void onProgress(long bytesWrite, long totalBytes, boolean isDone);
}
