package com.eric.core.lnterceptor;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author li
 * @Package com.eric.core.lnterceptor
 * @Title: LoggerInterceptor
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 日志模块打印
 */
public class LoggerInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Log.e("request Begin", String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers
                ()));
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            Charset charset = null;
            if (mediaType != null) {
                charset = mediaType.charset();
            }
            Log.e("body Params", String.format("MediaType %s%ncontentLength %s%ncharset %s", mediaType, requestBody.contentLength(), charset == null ? null : charset.displayName()));
        }
        long t1 = System.nanoTime();
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();
        if (response != null) {
            Log.e("response Get", String.format("Received response for %s in %.1fms%n%s" + "", response.request().url(), (t2 - t1)
                    / 1e6d, response.headers()));
        }
        return response;
    }
}
