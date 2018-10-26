package com.eric.core.core;

import android.content.Context;
import android.text.TextUtils;

import com.eric.core.listener.DownLoadListener;
import com.eric.core.listener.UploadListener;
import com.eric.core.listener.WebCallbackListener;
import com.eric.core.lnterceptor.LoggerInterceptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author li
 * @Package com.eric.core.core
 * @Title: HttpClientManager
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * Http引擎核心模块，OKHttp实现
 */
public class HttpClientManager {
    private Context context;

    private OkHttpClient mOkHttpClient;

    private static final String CHARSET_NAME = "UTF-8";

    private static final MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8");

    private boolean isKeepConnection;

    private HttpClientManager() {

    }

    /**
     * 初始化HttpClientManager
     *
     * @param context          上下文
     * @param isDebug          是否为debug模式，会影响指纹获取接口的URL，以及Doggy的秘钥和是否打印http请求日志
     * @param connectTimeout   连接超时时间
     * @param soTimeout        响应超时时间
     * @param isRetry          是否允许重试
     * @param isKeepConnection 是否保持KeepAlive
     */

    public void init(Context context, boolean isDebug, long connectTimeout, long soTimeout, boolean isRetry, boolean
            isKeepConnection) {
        this.context = context;
        this.isKeepConnection = isKeepConnection;
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(connectTimeout, TimeUnit.SECONDS).writeTimeout
                (soTimeout, TimeUnit.SECONDS).readTimeout(soTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(isRetry);

//        builder.addInterceptor(new GZipInterceptor());
        if (isDebug) {
            builder.addInterceptor(new LoggerInterceptor());
        }
        mOkHttpClient = builder.build();
    }


    private static class HttpClientManagerHolder {
        private static final HttpClientManager INSTANCE = new HttpClientManager();
    }

    public static final HttpClientManager getInstance() {
        return HttpClientManagerHolder.INSTANCE;
    }

    private String mapToStringParams(Map<String, String> params) throws UnsupportedEncodingException {
        String queryString = "";
        if (params != null) {
            for (String key : params.keySet()) {
                if (TextUtils.isEmpty(key)) {
                    throw new UnsupportedEncodingException("params is illegal");
                }
                String value = params.get(key);
                queryString += URLEncoder.encode(key, CHARSET_NAME) + "=" + URLEncoder.encode(TextUtils.isEmpty(value) ? "" :
                        value, CHARSET_NAME) + "&";
            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
        if (queryString.length() > 0) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        return queryString;
    }

    private boolean checkUrl(String url) {
        return TextUtils.isEmpty(url) || url.contains("?");
    }

    private void addHeader(Map<String, String> headers, Request.Builder builder, boolean iskeepAlive) throws
            IllegalArgumentException {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (TextUtils.isEmpty(entry.getKey())) {
                    throw new IllegalArgumentException("header is illegal");
                }
                builder.header(entry.getKey(), TextUtils.isEmpty(entry.getValue()) ? "" : entry.getValue());
            }
        }
        if (isKeepConnection) {
            return;
        }
        if (!iskeepAlive) {
            builder.header("Connection", "close");
        }

    }

    /**
     * 同步get请求
     *
     * @param url       http请求的url（PS:不包括url参数）
     * @param tag       代表的业务请求编码
     * @param headers   http请求头
     * @param urlParams url参数
     * @return SyncResponse 响应体
     */
    public SyncResponse get(String url, final Object tag, Map<String, String> headers, Map<String, String> urlParams) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setTag(tag);
        if (checkUrl(url)) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(new Exception("url is illegal"));
            return syncResponse;
        }
        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(e);
            return syncResponse;
        }
        Request.Builder builder = new Request.Builder().tag(tag).get();

        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }
        try {
            addHeader(headers, builder, false);
        } catch (IllegalArgumentException e) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(e);
        }
        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            syncResponse.setSuccess(response.isSuccessful());
            syncResponse.setData(response.body().string());
            syncResponse.setHttpCode(response.code());
            return syncResponse;
        } catch (IOException e) {
            syncResponse.setSuccess(false);
            syncResponse.setHttpCode(-1);
            syncResponse.setException(e);
            return syncResponse;
        }

    }

    /**
     * 同步post请求
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param bodyParams 请求体参数
     * @param isGzip     是否GZIP压缩
     * @return SyncResponse 响应体
     */
    public SyncResponse post(String url, final Object tag, Map<String, String> headers, Map<String, String> urlParams,
                             Map<String, String> bodyParams, boolean isGzip) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setTag(tag);
        if (checkUrl(url)) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(new Exception("url is illegal"));
            return syncResponse;
        }

        RequestBody requestBody;
        try {
            requestBody = RequestBody.create(FORM_URLENCODED, mapToStringParams(bodyParams));
        } catch (UnsupportedEncodingException e) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(e);
            return syncResponse;
        }
        Request.Builder builder = new Request.Builder().tag(tag).post(requestBody);

        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(e);
            return syncResponse;
        }
        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }
        try {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (isGzip) {
                headers.put("Content-Encoding", "gzip");
            }
            addHeader(headers, builder, false);
        } catch (IllegalArgumentException e) {
            syncResponse.setHttpCode(-1);
            syncResponse.setSuccess(false);
            syncResponse.setException(e);
            return syncResponse;
        }

        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            syncResponse.setSuccess(response.isSuccessful());
            syncResponse.setData(response.body().string());
            syncResponse.setHttpCode(response.code());
            return syncResponse;
        } catch (IOException e) {
            syncResponse.setSuccess(false);
            syncResponse.setHttpCode(-1);
            syncResponse.setException(e);
            return syncResponse;
        }


    }

    /**
     * 异步get请求
     *
     * @param url       http请求的url（PS:不包括url参数）
     * @param tag       代表的业务请求编码
     * @param headers   http请求头
     * @param urlParams url参数
     * @return CallHandle 响应句柄
     */
    public CallHandle asyncGet(final String url, final Object tag, Map<String, String> headers, Map<String, String> urlParams,
                               final WebCallbackListener<String> listener) {
        CallHandle callHandle = new CallHandle();
        if (checkUrl(url)) {
            listener.onFailure(tag, new Exception("url is illegal"));
            return callHandle;
        }
        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        Request.Builder builder = new Request.Builder().tag(tag).get();

        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }
        try {
            addHeader(headers, builder, false);
        } catch (IllegalArgumentException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, e);
                    }

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (listener != null && response != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess(tag, response.code(), response.body().string(), response.headers());
                        } else {
                            listener.onData(tag, response.code(), response.body().string());
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            listener.onFailure(tag, e);
        }
        callHandle.setCall(call);
        return callHandle;
    }

    /**
     * 异步post请求
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param bodyParams 请求体参数
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public CallHandle asyncPost(final String url, final Object tag, Map<String, String> headers, Map<String, String> urlParams,
                                Map<String, String> bodyParams, final WebCallbackListener<String> listener, boolean isGzip) {
        CallHandle callHandle = new CallHandle();
        if (checkUrl(url)) {
            listener.onFailure(tag, new Exception("url is illegal"));
            return callHandle;
        }

        RequestBody requestBody;
        try {
            requestBody = RequestBody.create(FORM_URLENCODED, mapToStringParams(bodyParams));
        } catch (UnsupportedEncodingException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        Request.Builder builder = new Request.Builder().tag(tag).post(requestBody);

        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }
        try {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (isGzip) {
                headers.put("Content-Encoding", "gzip");
            }
            addHeader(headers, builder, false);
        } catch (IllegalArgumentException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }

        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, e);
                    }

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (listener != null && response != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess(tag, response.code(), response.body().string(), response.headers());
                        } else {
                            listener.onData(tag, response.code(), response.body().string());
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            listener.onFailure(tag, e);
        }
        callHandle.setCall(call);
        return callHandle;

    }

    /**
     * 异步post json格式请求
     *
     * @param url      http请求的url（PS:不包括url参数）
     * @param tag      代表的业务请求编码
     * @param headers  http请求头
     * @param json     请求体参数
     * @param listener 回调监听
     * @param isGzip   是否GZIP压缩
     * @return CallHandle  响应句柄
     */
    public CallHandle asyncPostJson(String url, final Object tag, Map<String, String> headers, String json, final
    WebCallbackListener<String> listener, boolean isGzip) {
        CallHandle callHandle = new CallHandle();
        if (checkUrl(url)) {
            listener.onFailure(tag, new Exception("url is illegal"));
            return callHandle;
        }

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        if (json == null) {
            listener.onFailure(tag, new IllegalArgumentException("json body empty is not allowed"));
            return callHandle;
        }
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder().tag(tag).post(requestBody);
        builder.url(url);
        try {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (isGzip) {
                headers.put("Content-Encoding", "gzip");
            }
            addHeader(headers, builder, false);
        } catch (IllegalArgumentException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, e);
                    }

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (listener != null && response != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess(tag, response.code(), response.body().string(), response.headers());
                        } else {
                            listener.onData(tag, response.code(), response.body().string());
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            listener.onFailure(tag, e);
        }
        callHandle.setCall(call);
        return callHandle;

    }

    /**
     * 上传图片 post MultiPart格式
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param bodyParams 请求体数据域
     * @param files      请求体文件域
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public CallHandle asyncMultiPartUpload(final String url, final Object tag, Map<String, String> headers, Map<String, String>
            urlParams, Map<String, String> bodyParams, List<FileEntity> files, final UploadListener<String> listener, boolean
                                                   isGzip) {
        CallHandle callHandle = new CallHandle();
        if (checkUrl(url)) {
            listener.onFailure(tag, new Exception("url is illegal"));
            return callHandle;
        }

        MultipartBody.Builder multiBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (bodyParams != null) {
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                if (TextUtils.isEmpty(entry.getKey())) {
                    listener.onFailure(tag, new IllegalArgumentException("params is illegal"));
                    return callHandle;
                }
                multiBuilder.addFormDataPart(entry.getKey(), TextUtils.isEmpty(entry.getValue()) ? "" : entry.getValue());
            }
        }

        if (files != null) {
            for (FileEntity entity : files) {
                if (entity == null) {
                    listener.onFailure(tag, new IllegalArgumentException("params is illegal"));
                    return callHandle;
                }
                if ((TextUtils.isEmpty(entity.getName())) || (TextUtils.isEmpty(entity.getFileName())) || (entity.getFile() ==
                        null)) {
                    listener.onFailure(tag, new IllegalArgumentException("params is illegal"));
                    return callHandle;
                }
                multiBuilder.addFormDataPart(entity.getName(), entity.getFileName(), RequestBody.create(MediaType.parse
                        ("application/octet-stream"), entity.getFile()));
            }
        }
        MultipartBody multipartBody;
        try {
            multipartBody = multiBuilder.build();
        } catch (IllegalStateException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(multipartBody, listener);

        Request.Builder builder = new Request.Builder().tag(tag).post(progressRequestBody);

        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }
        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }

        try {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (isGzip) {
                headers.put("Content-Encoding", "gzip");
            }
            addHeader(headers, builder, true);
        } catch (IllegalArgumentException e) {
            listener.onFailure(tag, e);
            return callHandle;
        }

        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, e);
                    }

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (listener != null && response != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess(tag, response.code(), response.body().string(), response.headers());
                        } else {
                            listener.onData(tag, response.code(), response.body().string());
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            listener.onFailure(tag, e);
        }
        callHandle.setCall(call);
        return callHandle;

    }

    /**
     * get请求 下载文件(支持断点续传)
     *
     * @param url       http请求的url（PS:不包括url参数）
     * @param tag       代表的业务请求编码
     * @param destPath  下载后存储的目标路径
     * @param headers   http请求头
     * @param urlParams url参数
     * @param listener  回调监听
     * @param isRange   是否开启断点续传
     * @return CallHandle 响应句柄
     */
    public CallHandle asyncDownLoad(final String url, final Object tag, final String destPath, Map<String, String> headers,
                                    Map<String, String> urlParams, final DownLoadListener listener, final boolean isRange) {
        CallHandle callHandle = new CallHandle();
        if (checkUrl(url)) {
            if (listener != null) {
                listener.onFailure(tag, new Exception("url is illegal"));
            }
            return callHandle;
        }

        String sParams;
        try {
            sParams = mapToStringParams(urlParams);
        } catch (UnsupportedEncodingException e) {
            if (listener != null) {
                listener.onFailure(tag, e);
            }
            return callHandle;
        }
        if (TextUtils.isEmpty(destPath)) {
            if (listener != null) {
                listener.onFailure(tag, new Exception("dest path empty is not allowed"));
            }
            return callHandle;
        }
        final File file = new File(destPath);
        if (file.isDirectory()) {
            if (listener != null) {
                listener.onFailure(tag, new Exception("dest path does not support for directory,only absolute file path is " +
                        "allowed"));

            }
            return callHandle;
        }
        if (!file.exists()) {
            boolean b;
            if (!file.getParentFile().exists()) {
                b = file.getParentFile().mkdirs();
                if (!b) {
                    if (listener != null) {
                        listener.onFailure(tag, new Exception("destPath permission denied!"));
                    }
                    return callHandle;
                } else {
                    try {
                        boolean isCreate = file.createNewFile();
                        if (!isCreate) {
                            if (listener != null) {
                                listener.onFailure(tag, new Exception("destPath permission denied!"));
                            }
                            return callHandle;
                        }
                    } catch (IOException e) {
                        if (listener != null) {
                            listener.onFailure(tag, new Exception("destPath permission denied!"));
                        }
                        return callHandle;
                    }
                }
            } else {
                try {
                    boolean isCreate = file.createNewFile();
                    if (!isCreate) {
                        if (listener != null) {
                            listener.onFailure(tag, new Exception("destPath permission denied!"));
                        }
                        return callHandle;
                    }
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, new Exception("destPath permission denied!"));
                    }
                    return callHandle;
                }
            }
        }
        Request.Builder builder = new Request.Builder().tag(tag);

        if (TextUtils.isEmpty(sParams)) {
            builder.url(url);
        } else {
            builder.url(url + "?" + sParams);
        }

        try {
            addHeader(headers, builder, true);
            if (isRange) {
                if (file.length() > 0) {
                    builder.header("Range", "bytes=" + file.length() + "-");
                }
            }

        } catch (IllegalArgumentException e) {
            if (listener != null) {
                listener.onFailure(tag, e);
            }

            return callHandle;
        }

        Request request = builder.build();
        OkHttpClient.Builder okBuilder = mOkHttpClient.newBuilder();
        if (!isRange) {
            okBuilder.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    //拦截
                    Response originalResponse = chain.proceed(chain.request());
                    //包装响应体并返回
                    return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), listener))
                            .build();
                }
            });
        }
        Call call = okBuilder.build().newCall(request);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(tag, e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response != null) {
                        if (response.isSuccessful()) {
                            BufferedInputStream reader = null;
                            RandomAccessFile rSavedFile = null;
                            try {
                                reader = new BufferedInputStream(response.body().byteStream());
                                rSavedFile = new RandomAccessFile(destPath, "rw");
                                if (isRange) {
                                    if (response.code() == 206) {
                                        rSavedFile.seek(file.length());
                                    }
                                }
                                int n;
                                byte[] buffer = new byte[1024 * 8];
                                long fullLength = 0L;
                                if (isRange) {
                                    fullLength = response.body().contentLength() + rSavedFile.length();
                                }

                                while ((n = reader.read(buffer, 0, buffer.length)) != -1) {
                                    rSavedFile.write(buffer, 0, n);
                                    if (isRange) {
                                        if (listener != null) {
                                            listener.onProgress(rSavedFile.length(), fullLength, rSavedFile.length() ==
                                                    fullLength);
                                        }
                                    }
                                }

                                if (listener != null) {
                                    listener.onSuccess(tag, response.code(), file);
                                }

                            } catch (Exception e) {
                                if (listener != null) {
                                    listener.onFailure(tag, new Exception("destPath permission denied!"));
                                }
                            } finally {
                                if (rSavedFile != null) {
                                    rSavedFile.close();
                                }
                                if (reader != null) {
                                    reader.close();
                                }

                            }

                        } else {
                            if (file.exists()) {
                                file.delete();
                            }
                            if (listener != null) {
                                listener.onData(tag, response.code(), response.body().string());
                            }
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            if (listener != null) {
                listener.onFailure(tag, e);
            }

        }
        callHandle.setCall(call);
        return callHandle;
    }

    /**
     * 根据网络请求标识位取消网络请求
     *
     * @param tag 网络请求标识位
     */
    public void cancelByTag(Object tag) {
        Dispatcher dispatcher = mOkHttpClient.dispatcher();
        synchronized (dispatcher) {
            for (Call call : dispatcher.queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
            for (Call call : dispatcher.runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }
}
