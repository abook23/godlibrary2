package com.god.retrofit.initerceptor;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by abook23 on 2016/11/21.
 * Versions 1.0
 */

public class LoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String TAG = "okhttp_log";
    private LogModel mLogModel;

    public enum LogModel {
        ALL, CONCISE, NONE
    }

    public LoggingInterceptor() {
        this.mLogModel = LogModel.CONCISE;
    }

    public LoggingInterceptor(LogModel logModel) {
        this.mLogModel = logModel;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        long t1 = System.nanoTime();
        Response response = chain.proceed(chain.request());
        long t2 = System.nanoTime();
        if (mLogModel == LogModel.NONE) {
            return response;
        }
        try {
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType mediaType = responseBody.contentType();
            if (mediaType != null) {
                charset = mediaType.charset(UTF8);
            }
            String content = buffer.clone().readString(charset);
            switch (mLogModel) {
                case ALL:
                    Log.i(TAG, String.format(Locale.getDefault(), "Received response for %s in %.1fms%n%s%n%s",
                            response.request().url(), (t2 - t1) / 1e6d, response.headers(), content));
                    break;
                case CONCISE:
                    Log.i(TAG, String.format(Locale.getDefault(), "%s in %.1fms%n%s",
                            response.request().url(), (t2 - t1) / 1e6d, content));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        okhttp3.MediaType mediaType = response.body().contentType();
//        String content = response.body().string();
//        return response.newBuilder()
//                .body(okhttp3.ResponseBody.create(mediaType, content))
//                .build();
        return response;
    }
}
