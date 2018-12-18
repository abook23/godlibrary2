package com.god.retrofit.api;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by abook23 on 2016/12/2.
 */

public interface FileApi {

    @POST()
    Observable<ResponseBody> uploading(@Url String url, @Body MultipartBody multipartBody);

    @Streaming
    @GET()
    Observable<ResponseBody> download(@Url() String url);
}
