package com.eric.http.error;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

/**
 * @author li
 * @Package com.eric.http.error
 * @Title: ErrorParser
 * @Description: Copyright (c)
 * Create DateTime: 2017/11/29
 */
public class ErrorParser {
    public static String parse(int httpCode) {
        String errorMsg = "未知错误";
        switch (httpCode) {
            case 400:
                errorMsg = "400:Bad Request,请求参数不合法";
                break;
            case 401:
                errorMsg = "401:Unauthorized,未授权,要求身份验证";
                break;
            case 403:
                errorMsg = "403:服务器拒绝请求";
                break;
            case 404:
                errorMsg = "404:您请求的资源去火星了,囧";
                break;
            case 408:
                errorMsg = "408:请求超时";
                break;
            case 413:
                errorMsg = "413:请求实体过大";
                break;
            case 414:
                errorMsg = "413:请求的 URI 过长";
                break;
            case 415:
                errorMsg = "415:目标资源不支持该媒体类型";
                break;
            case 500:
                errorMsg = "500:服务器内部错误";
                break;
            case 501:
                errorMsg = "501:服务器内部错误";
                break;
            case 502:
                errorMsg = "502:网关异常";
                break;
            case 503:
                errorMsg = "503:服务器正忙或正在维护,目前不可用";
                break;
            case 504:
                errorMsg = "504:网关超时";
                break;
            case 505:
                errorMsg = "505:HTTP 版本不受支持";
                break;
            default:
                break;
        }
        return errorMsg;
    }

    public static Exception parse(Exception e) {
        Exception exception = new Exception("请求失败", e.getCause());
        if (e instanceof CertificateExpiredException) {
            exception = new Exception("您的时间不正确，请检查时间设置!", e.getCause());
        } else if (e instanceof CertificateNotYetValidException) {
            exception = new Exception("您的时间不正确，请检查时间设置!", e.getCause());
        } else if (e instanceof ConnectException) {
            exception = new Exception("网络请求超时!", e.getCause());
        } else if (e instanceof SocketTimeoutException) {
            exception = new Exception("网络响应超时!", e.getCause());
        }

        return exception;

    }
}
