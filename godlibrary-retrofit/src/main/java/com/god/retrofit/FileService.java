package com.god.retrofit;

import android.content.Context;

import com.god.retrofit.api.FileApi;
import com.god.retrofit.initerceptor.LoggingInterceptor;
import com.god.retrofit.progress.OnDownloadListener;
import com.god.retrofit.progress.OnUpLoadingListener;
import com.god.retrofit.progress.ProgressRequestBody;
import com.god.retrofit.progress.ProgressResponseBody;
import com.god.retrofit.rxjava.RxJavaUtils;
import com.god.retrofit.util.AppUtils;
import com.god.retrofit.util.FileUtils;
import com.god.retrofit.util.MultipartUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by abook23 on 2016/11/22.
 * Versions 1.0
 */

public class FileService {

    private String baseUrl;
    private static FileService SERVICE;
    public static boolean DEBUG = true;
    public static long connectTimeout_SECONDS = 60;
    public static long readTimeout_SECONDS = 600;
    public static long writeTimeout_SECONDS = 600;

    public static FileService init(Context applicationContext, String baseUrl) {
        SERVICE = new FileService();
        SERVICE.baseUrl = baseUrl;
        AppUtils.initial(applicationContext);
        return SERVICE;
    }

    public static FileService getInit() {
        if (SERVICE == null)
            throw new NullPointerException("请初始化 FileService.init(Context applicationContext, String baseUrl)");
        return SERVICE;
    }

    private Retrofit.Builder getBuilder() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());
    }


    public <T> T create(Class<T> tClass) {
        OkHttpClient.Builder builder = getOkHttpBuilder();
        return SERVICE.getBuilder()
                .client(builder.build())
                .build()
                .create(tClass);
    }

    /**
     * 创建带响应进度(下载进度)回调的service
     */
    public <T> T create(Class<T> tClass, final OnDownloadListener listener) {
        OkHttpClient.Builder builder = getOkHttpBuilder();
        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截
                Response originalResponse = chain.proceed(chain.request());
                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), listener))
                        .build();
            }
        });
        return SERVICE.getBuilder()
                .client(builder.build())
                .build()
                .create(tClass);
    }

    /**
     * 创建带请求体进度(上传进度)回调的service
     */
    public <T> T create(Class<T> tClass, final OnUpLoadingListener listener) {
        OkHttpClient.Builder builder = getOkHttpBuilder();
        //增加拦截器
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .method(original.method(), new ProgressRequestBody(original.body(), listener))
                        .build();
                return chain.proceed(request);
            }
        });
        return SERVICE.getBuilder()
                .client(builder.build())
                .build()
                .create(tClass);
    }

    private OkHttpClient.Builder getOkHttpBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        setTimeOut(builder);
        return builder;
    }

    private void setTimeOut(OkHttpClient.Builder builder) {
        builder.connectTimeout(connectTimeout_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(readTimeout_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(writeTimeout_SECONDS, TimeUnit.SECONDS);
    }

    public Observable<ResponseBody> upload(String url, File... files) {
        if (files.length == 1) {
            return getInit().create(FileApi.class)
                    .uploading(url, MultipartUtils.filesToMultipartBody(files[0]))
                    .compose(RxJavaUtils.<ResponseBody>defaultSchedulers());
        } else {
            return upload(url, Arrays.asList(files));
        }
    }

    public Observable<ResponseBody> upload(String url, List<File> files) {
        return getInit().create(FileApi.class)
                .uploading(url, MultipartUtils.filesToMultipartBody(files))
                .compose(RxJavaUtils.<ResponseBody>defaultSchedulers());
    }

    public Observable<File> download(final String url) {
        final String fileName = url.substring(url.lastIndexOf("/") + 1);
        return com.god.retrofit.FileService.getInit().create(FileApi.class).download(url).map(new Func1<ResponseBody, File>() {
            @Override
            public File call(ResponseBody responseBody) {
                return FileUtils.saveFile(responseBody.byteStream(), FileUtils.getDowloadDir(AppUtils.getApplicationContext()), fileName);
            }
        }).compose(RxJavaUtils.<File>defaultSchedulers());
    }
}
