package com.eric.core.core;

/**
 * @author li
 * @Package com.eric.core.core
 * @Title: SyncResponse
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 同步请求对应实体类
 */
public class SyncResponse {
    /**
     * 业务标识
     */
    private Object tag;
    /**
     * httpCode返回>=200,<300的时候返回true,否则返回false，httpCode=-1时也是false
     */
    private boolean isSuccess;
    /**
     * 服务端返回数据
     */
    private String data;
    /**
     * http响应码 -1代表服务端无响应，会抛Exception
     */
    private int httpCode;
    /**
     * 出现异常
     */
    private Exception exception;

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
