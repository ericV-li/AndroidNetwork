package com.eric.core.core;

import okhttp3.Call;

/**
 * @author li
 * @Package com.eric.core.core
 * @Title: CallHandle
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * http请求返回的句柄，可用于取消请求
 */
public class CallHandle {
    private Call call;

    protected void setCall(Call call) {
        this.call = call;
    }

    /**
     * 取消网络请求
     */
    public void cancel() {
        if (call == null) {
            return;
        }
        call.cancel();
    }

    /**
     * 判定网络请求是否正在执行
     *
     * @return
     */
    public boolean isExecuted() {
        if (call == null) {
            return false;
        }
        return call.isExecuted();
    }

    /**
     * 判定完全请求是否取消
     *
     * @return
     */
    public boolean isCanceled() {
        if (call == null) {
            return false;
        }
        return call.isCanceled();
    }
}
