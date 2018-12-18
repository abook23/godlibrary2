package com.god.retrofit;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.god.retrofit.listener.loading.Call;
import com.god.retrofit.progress.OnUpLoadingListener;
import com.god.retrofit.rxjava.ObserverBaseWeb;
import com.god.retrofit.rxjava.ResponseCodeError;
import com.god.retrofit.util.MultipartUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by abook23 on 2016/11/25.
 * Versions 1.0
 */

public class UploadFile {

    private Call mCall;
    private boolean pause;
    private boolean cancel;
    private boolean isStart;

    private static final int KEY_START = 0x01;
    private static final int KEY_SIZE = 0x02;
    private List<File> mFiles = new ArrayList<>();
    private String mUrl;

    public UploadFile(String url, File file) {
        mUrl = url;
        mFiles.add(file);
    }

    public UploadFile(String url, List<File> files) {
        mUrl = url;
        mFiles = files;
    }

    public void setOnListener(Call call) {
        mCall = call;
    }

    public void start() {
        upload(mUrl, mFiles);
    }

    private void upload(String url, List<File> files) {
        if (mCall != null) {
            mCall.onStart();
            isStart = true;
        }
        FileService.getInit().create(Api.class, new OnUpLoadingListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (!isStart && mCall != null) {
                    isStart = true;
                    mHandler.obtainMessage(KEY_START).sendToTarget();
                }
                if (mCall != null)
                    mHandler.obtainMessage(KEY_SIZE, new long[]{bytesRead, contentLength}).sendToTarget();
                while (pause) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (cancel) {
                    throw new ResponseCodeError("cancel");
                }
            }
        }).uploading(url, MultipartUtils.filesToMultipartBody(files))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverBaseWeb<ResponseBody>() {
                               @Override
                               public void onNext(ResponseBody responseBody) {
                                   mCall.onSuccess(responseBody);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   super.onError(e);
                                   if (!cancel && mCall != null) {
                                       mCall.onFail(e);
                                   }
                               }
                           }

                );
    }

    public boolean isPause() {
        return pause;
    }

    public void pause() {
        pause = true;
        mCall.onPause();
    }

    public void resume() {
        pause = false;
        mCall.onResume();
    }

    public void cancel() {
        cancel = true;
        pause = false;
        mCall.onCancel();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY_START:
                    mCall.onStart();
                    break;
                case KEY_SIZE:
                    long[] values = (long[]) msg.obj;
                    mCall.onSize(values[0], values[1]);
                    break;
            }
        }
    };

    private interface Api {
        @POST()
        Observable<ResponseBody> uploading(@Url String url, @Body MultipartBody multipartBody);
    }
}
