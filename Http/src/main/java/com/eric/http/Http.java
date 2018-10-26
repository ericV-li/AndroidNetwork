package com.eric.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.eric.core.core.CallHandle;
import com.eric.core.core.FileEntity;
import com.eric.core.core.HttpClientManager;
import com.eric.core.core.SyncResponse;
import com.eric.core.listener.DownLoadListener;
import com.eric.core.listener.UploadListener;
import com.eric.core.listener.WebCallbackListener;
import com.eric.http.error.ErrorParser;
import com.eric.http.utils.CompatErrorUtil;
import com.eric.http.utils.HttpSerializer;
import com.eric.http.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Headers;

/**
 * @author li
 * @Package com.eric.http
 * @Title: Http
 * @Description: Copyright (c)
 * Create DateTime: 2017/11/29
 * 提供上层调用网络代理类
 */
public class Http {
    private Context context;
    private boolean isDebug;
    private String netWorkTip = "当前网络不可用，请检查";

    private Http() {

    }

    private static class LJHttpHolder {
        private static final Http INSTANCE = new Http();
    }

    public static final Http getInstance() {
        return LJHttpHolder.INSTANCE;
    }

    /**
     * 初始化LJHttp
     *
     * @param isDebug        是否为debug模式，会影响指纹获取接口的URL，以及Doggy的秘钥和是否打印http请求日志
     * @param connectTimeout 连接超时时间
     * @param soTimeout      响应超时时间
     * @param isRetry        是否允许重试
     */
    public void init(Context context, boolean isDebug, long connectTimeout, long soTimeout, boolean isRetry) {
        this.context = context;
        this.isDebug = isDebug;
        HttpClientManager.getInstance().init(context, isDebug, connectTimeout, soTimeout, isRetry, false);
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
        SyncResponse sr = syncCheckNetwork(tag);
        if (sr != null) {
            return sr;
        }
        SyncResponse syncResponse = HttpClientManager.getInstance().get(url, tag, headers, urlParams);
        if (syncResponse.getHttpCode() >= 400 && syncResponse.getHttpCode() < 600) {
            syncResponse.setData(ErrorParser.parse(syncResponse.getHttpCode()));
            return syncResponse;
        }
        if (syncResponse.getException() != null) {
            Exception exception = ErrorParser.parse(syncResponse.getException());
            syncResponse.setException(exception);
            return syncResponse;
        }
        syncResponse.setData(CompatErrorUtil.replaceJson(syncResponse.getData(), isDebug));
        return syncResponse;
    }

    /**
     * 同步post请求
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param urlParams  url参数
     * @param bodyParams 请求体参数
     * @param isGzip     是否GZIP压缩
     * @return SyncResponse 响应体
     */
    public SyncResponse post(String url, final Object tag, Map<String, String> headers, Map<String, String> urlParams,
                             Map<String, String> bodyParams, boolean isGzip) {
        SyncResponse sr = syncCheckNetwork(tag);
        if (sr != null) {
            return sr;
        }
        SyncResponse syncResponse = HttpClientManager.getInstance().post(url, tag, headers, urlParams, bodyParams, isGzip);
        if (syncResponse.getHttpCode() >= 400 && syncResponse.getHttpCode() < 600) {
            syncResponse.setData(ErrorParser.parse(syncResponse.getHttpCode()));
            return syncResponse;
        }
        if (syncResponse.getException() != null) {
            Exception exception = ErrorParser.parse(syncResponse.getException());
            syncResponse.setException(exception);
            return syncResponse;
        }
        syncResponse.setData(CompatErrorUtil.replaceJson(syncResponse.getData(), isDebug));
        return syncResponse;
    }

    /**
     * 异步get请求
     *
     * @param url       http请求的url（PS:不包括url参数）
     * @param tag       代表的业务请求编码
     * @param headers   http请求头
     * @param urlParams url参数
     * @param listener  回调接口
     * @return CallHandle 响应句柄
     */

    public <T> CallHandle asyncGet(final String url, final Object tag, Map<String, String> headers, final Map<String, String>
            urlParams, final Class<T> clazz, final WebCallbackListener<T> listener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncGet(url, tag, headers, urlParams, new
                WebCallbackListener<String>() {
                    @Override
                    public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {

                        try {
                            if (clazz == String.class) {
                                T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                                if (listener != null) {
                                    listener.onSuccess(tag, httpCode, t, headers);
                                }
                                return;
                            }
                            final T t = clazz.newInstance();
                            HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.onSuccess(tag, httpCode, t, headers);
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            if (clazz == String.class) {
                                if (listener != null) {
                                    listener.onFailure(tag, e);
                                }
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.onFailure(tag, e);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onData(final Object tag, final int httpCode, final String data) {
                        if (clazz == String.class) {
                            if (listener != null) {
                                if (httpCode >= 400 && httpCode < 600) {
                                    listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                    return;
                                }
                                listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                            }
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    if (httpCode >= 400 && httpCode < 600) {
                                        listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                        return;
                                    }
                                    listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Object tag, Exception e) {
                        final Exception exception;
                        exception = ErrorParser.parse(e);
                        if (clazz == String.class) {
                            if (listener != null) {
                                listener.onFailure(tag, exception);
                            }
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onFailure(tag, exception);
                                }
                            }
                        });
                    }
                });
    }

    /**
     * 异步post请求
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param urlParams  url参数
     * @param bodyParams 请求体参数
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public <T> CallHandle asyncPost(final String url, final Object tag, Map<String, String> headers, final Map<String, String>
            urlParams, final Map<String, String> bodyParams, final Class<T> clazz, final WebCallbackListener<T> listener, final
                                    boolean isGzip) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncPost(url, tag, headers, urlParams, bodyParams, new WebCallbackListener<String>() {
            @Override
            public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {
                try {
                    if (clazz == String.class) {
                        T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                        if (listener != null) {
                            listener.onSuccess(tag, httpCode, t, headers);
                        }
                        return;
                    }
                    final T t = clazz.newInstance();
                    HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSuccess(tag, httpCode, t, headers);
                            }
                        }
                    });
                } catch (final Exception e) {
                    if (clazz == String.class) {
                        if (listener != null) {
                            listener.onFailure(tag, e);
                        }
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(tag, e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onData(final Object tag, final int httpCode, final String data) {
                if (clazz == String.class) {
                    if (listener != null) {
                        if (httpCode >= 400 && httpCode < 600) {
                            listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                            return;
                        }
                        listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            if (httpCode >= 400 && httpCode < 600) {
                                listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                return;
                            }
                            listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                        }
                    }
                });
            }


            @Override
            public void onFailure(final Object tag, Exception e) {
                final Exception exception;
                exception = ErrorParser.parse(e);
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onFailure(tag, exception);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(tag, exception);
                        }
                    }
                });
            }
        }, isGzip);
    }

    /**
     * 异步post请求 ,不包含extra基础数据
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param urlParams  url参数
     * @param bodyParams 请求体参数
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public <T> CallHandle asyncPost2(final String url, final Object tag, Map<String, String> headers, final Map<String, String>
            urlParams, final Map<String, String> bodyParams, final Class<T> clazz, final WebCallbackListener<T> listener, final
                                     boolean isGzip) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncPost(url, tag, headers, urlParams, bodyParams, new
                WebCallbackListener<String>() {
                    @Override
                    public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {
                        try {
                            if (clazz == String.class) {
                                T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                                if (listener != null) {
                                    listener.onSuccess(tag, httpCode, t, headers);
                                }
                                return;
                            }
                            final T t = clazz.newInstance();
                            HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.onSuccess(tag, httpCode, t, headers);
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            if (clazz == String.class) {
                                if (listener != null) {
                                    listener.onFailure(tag, e);
                                }
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.onFailure(tag, e);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onData(final Object tag, final int httpCode, final String data) {
                        if (clazz == String.class) {
                            if (listener != null) {
                                if (httpCode >= 400 && httpCode < 600) {
                                    listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                    return;
                                }
                                listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                            }
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    if (httpCode >= 400 && httpCode < 600) {
                                        listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                        return;
                                    }
                                    listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                                }
                            }
                        });
                    }


                    @Override
                    public void onFailure(final Object tag, Exception e) {
                        final Exception exception;
                        exception = ErrorParser.parse(e);
                        if (clazz == String.class) {
                            if (listener != null) {
                                listener.onFailure(tag, exception);
                            }
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onFailure(tag, exception);
                                }
                            }
                        });
                    }
                }, isGzip);
    }

    /**
     * 异步post json格式请求
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param jsonObject 请求体参数
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle  响应句柄
     */
    public <T> CallHandle asyncPostJson(final String url, final Object tag, Map<String, String> headers, JSONObject jsonObject,
                                        final Class<T> clazz, final WebCallbackListener<T> listener, final boolean isGzip) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        CallHandle callHandle = new CallHandle();
        final String params = jsonObject == null ? "" : jsonObject.toString();
        final Map<String, String> trackParam = new HashMap<>();
        trackParam.put("jsonObject", params);
        callHandle = HttpClientManager.getInstance().asyncPostJson(url, tag, headers, params, new WebCallbackListener<String>() {
            @Override
            public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {
                try {
                    if (clazz == String.class) {
                        T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                        if (listener != null) {
                            listener.onSuccess(tag, httpCode, t, headers);
                        }
                        return;
                    }
                    final T t = clazz.newInstance();
                    HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSuccess(tag, httpCode, t, headers);
                            }
                        }
                    });
                } catch (final Exception e) {
                    if (clazz == String.class) {
                        if (listener != null) {
                            listener.onFailure(tag, e);
                        }
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(tag, e);
                            }
                        }
                    });
                }

            }

            @Override
            public void onData(final Object tag, final int httpCode, final String data) {
                if (clazz == String.class) {
                    if (listener != null) {
                        if (httpCode >= 400 && httpCode < 600) {
                            listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                            return;
                        }
                        listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            if (httpCode >= 400 && httpCode < 600) {
                                listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                return;
                            }
                            listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Object tag, Exception e) {
                final Exception exception;
                exception = ErrorParser.parse(e);
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onFailure(tag, exception);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(tag, exception);
                        }
                    }
                });
            }
        }, isGzip);
        return callHandle;
    }

    /**
     * 上传图片 post MultiPart格式
     *
     * @param url        http请求的url（PS:不包括url参数）
     * @param tag        代表的业务请求编码
     * @param headers    http请求头
     * @param urlParams  url参数
     * @param bodyParams 请求体数据域
     * @param files      请求体文件域
     * @param listener   回调监听
     * @param isGzip     是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public <T> CallHandle asyncMultiPartUpload(final String url, final Object tag, Map<String, String> headers, final
    Map<String, String> urlParams, final Map<String, String> bodyParams, final List<FileEntity> files, final Class<T> clazz,
                                               final UploadListener<T> listener, final boolean isGzip) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncMultiPartUpload(url, tag, headers, urlParams, bodyParams, files, new UploadListener<String>() {


            @Override
            public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {
                String fileSize = "";
                if (files != null) {
                    try {
                        for (FileEntity entity : files) {
                            String path = entity.getFile().getAbsolutePath();
                            File file = new File(path);
                            if (file.exists()) {
                                fileSize += "-" + file.length();
                            }
                        }
                    } catch (Exception e) {
                        // empty here
                    }
                }
                try {
                    if (clazz == String.class) {
                        T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                        if (listener != null) {
                            listener.onSuccess(tag, httpCode, t, headers);
                        }
                        return;
                    }
                    final T t = clazz.newInstance();
                    HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSuccess(tag, httpCode, t, headers);
                            }
                        }
                    });
                } catch (final Exception e) {
                    if (clazz == String.class) {
                        if (listener != null) {
                            listener.onFailure(tag, e);
                        }
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(tag, e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onData(final Object tag, final int httpCode, final String data) {
                if (clazz == String.class) {
                    if (listener != null) {
                        if (httpCode >= 400 && httpCode < 600) {
                            listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                            return;
                        }
                        listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            if (httpCode >= 400 && httpCode < 600) {
                                listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                return;
                            }
                            listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Object tag, Exception e) {
                final Exception exception;
                exception = ErrorParser.parse(e);
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onFailure(tag, exception);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(tag, exception);
                        }
                    }
                });
            }

            @Override
            public void onProgress(final long bytesWrite, final long totalBytes, final boolean isDone) {
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onProgress(bytesWrite, totalBytes, isDone);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onProgress(bytesWrite, totalBytes, isDone);
                        }
                    }
                });
            }
        }, isGzip);
    }

    /**
     * get请求 下载文件
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
                                    final Map<String, String> urlParams, final DownLoadListener listener, final boolean isRange) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncDownLoad(url, tag, destPath, headers, urlParams, new DownLoadListener() {
            @Override
            public void onSuccess(final Object tag, final int httpCode, final File file) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onSuccess(tag, httpCode, file);
                        }
                    }
                });
            }


            @Override
            public void onData(final Object tag, final int httpCode, final String data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            if (httpCode >= 400 && httpCode < 600) {
                                listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                return;
                            }
                            listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Object tag, final Exception e) {
                final Exception exception;
                exception = ErrorParser.parse(e);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(tag, exception);
                        }
                    }
                });
            }

            @Override
            public void onProgress(final long bytesWrite, final long totalBytes, final boolean isDone) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onProgress(bytesWrite, totalBytes, isDone);
                        }
                    }
                });
            }
        }, isRange);
    }


    /**
     * 上传图片 post MultiPart格式
     *
     * @param url       http请求的url（PS:不包括url参数）
     * @param tag       代表的业务请求编码
     * @param headers   http请求头
     * @param urlParams url参数
     * @param files     请求体文件域
     * @param listener  回调监听
     * @param isGzip    是否GZIP压缩
     * @return CallHandle 响应句柄
     */
    public <T> CallHandle asyncMultiPartUploadExtra(final String url, final Object tag, Map<String, String> headers, final
    Map<String, String> urlParams, List<FileEntity> files, final Class<T> clazz, final UploadListener<T> listener, final
                                                    boolean isGzip) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!asyncCheckNetwork(tag, clazz, listener, handler)) {
            return new CallHandle();
        }
        return HttpClientManager.getInstance().asyncMultiPartUpload(url, tag, headers, urlParams, null, files, new UploadListener<String>() {
            @Override
            public void onSuccess(final Object tag, final int httpCode, final String result, final Headers headers) {
                try {
                    if (clazz == String.class) {
                        T t = (T) (CompatErrorUtil.replaceJson(result, isDebug));
                        if (listener != null) {
                            listener.onSuccess(tag, httpCode, t, headers);
                        }
                        return;
                    }
                    final T t = clazz.newInstance();
                    HttpSerializer.deserializeJSONObject(t, new JSONObject(CompatErrorUtil.replaceJson(result, isDebug)));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSuccess(tag, httpCode, t, headers);
                            }
                        }
                    });
                } catch (final Exception e) {
                    if (clazz == String.class) {
                        if (listener != null) {
                            listener.onFailure(tag, e);
                        }
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(tag, e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onData(final Object tag, final int httpCode, final String data) {
                if (clazz == String.class) {
                    if (listener != null) {
                        if (httpCode >= 400 && httpCode < 600) {
                            listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                            return;
                        }
                        listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            if (httpCode >= 400 && httpCode < 600) {
                                listener.onData(tag, httpCode, ErrorParser.parse(httpCode));
                                return;
                            }
                            listener.onData(tag, httpCode, CompatErrorUtil.replaceJson(data, isDebug));
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Object tag, final Exception e) {
                final Exception exception;
                exception = ErrorParser.parse(e);
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onFailure(tag, exception);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(tag, exception);
                        }
                    }
                });
            }

            @Override
            public void onProgress(final long bytesWrite, final long totalBytes, final boolean isDone) {
                if (clazz == String.class) {
                    if (listener != null) {
                        listener.onProgress(bytesWrite, totalBytes, isDone);
                    }
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onProgress(bytesWrite, totalBytes, isDone);
                        }
                    }
                });
            }
        }, isGzip);
    }


    private SyncResponse syncCheckNetwork(Object tag) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            SyncResponse syncResponse = new SyncResponse();
            syncResponse.setTag(tag);
            syncResponse.setSuccess(false);
            syncResponse.setHttpCode(-1);
            syncResponse.setException(new Exception(netWorkTip));
            return syncResponse;
        }
        return null;
    }

    private <T> boolean asyncCheckNetwork(final Object tag, Class<T> clazz, final WebCallbackListener<T> listener, Handler
            handler) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            if (clazz == String.class) {
                if (listener != null) {
                    listener.onFailure(tag, new Exception(netWorkTip));
                }
                return false;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onFailure(tag, new Exception(netWorkTip));
                    }
                }
            });
            return false;
        }
        return true;
    }

    private <T> boolean asyncCheckNetwork(final Object tag, Class<T> clazz, final UploadListener<T> listener, Handler handler) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            if (clazz == String.class) {
                if (listener != null) {
                    listener.onFailure(tag, new Exception(netWorkTip));
                }
                return false;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onFailure(tag, new Exception(netWorkTip));
                    }
                }
            });
            return false;
        }
        return true;
    }

    private boolean asyncCheckNetwork(final Object tag, final DownLoadListener listener, Handler handler) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onFailure(tag, new Exception(netWorkTip));
                    }
                }
            });
            return false;
        }
        return true;
    }

    /**
     * 根据网络请求标识位取消网络请求
     *
     * @param tag 网络请求标识位
     */
    public void cancelByTag(Object tag) {
        HttpClientManager.getInstance().cancelByTag(tag);
    }

    /**
     * 产生唯一网络标识符
     *
     * @return 网络标识符
     */
    public String generateTag() {
        return UUID.randomUUID().toString();
    }
}
