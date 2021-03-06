package com.god.retrofit;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.god.retrofit.listener.download.Call;
import com.god.retrofit.progress.OnDownloadListener;
import com.god.retrofit.rxjava.ObserverBaseWeb;
import com.god.retrofit.rxjava.ResponseCodeError;
import com.god.retrofit.util.AppUtils;
import com.god.retrofit.util.FileUtils;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by abook23 on 2016/11/25.
 * Versions 1.0
 */

public class DownloadFile {

    private Call mCall;
    private boolean mPause;
    private boolean cancel;
    private String mUrl;
    private static final int KEY_SIZE = 0x02;

    public void setCall(Call call) {
        mCall = call;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY_SIZE:
                    if (mCall != null)
                        mCall.onSize(msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    public DownloadFile(String url) {
        mUrl = url;
    }

    public void start() {
        if (mCall != null) {
            mCall.onStart();
        }
        final String parent = FileUtils.getDowloadDir(AppUtils.getApplicationContext());
        final String fileName = mUrl.substring(mUrl.lastIndexOf("/") + 1);
        FileService.getInit().create(Api.class, mOnDownloadListener).download(mUrl).map(new Func1<ResponseBody, File>() {
            @Override
            public File call(ResponseBody responseBody) {
                return FileUtils.saveFile(responseBody.byteStream(), parent, fileName);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ObserverBaseWeb<File>() {
            @Override
            public void onNext(File file) {
                if (mCall != null)
                    mCall.onSuccess(file);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if (!cancel && mCall != null)
                    mCall.onFail(e);
                String filePath = parent + File.separator + fileName;
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }

    public boolean isPause() {
        return mPause;
    }

    public void pause() {
        mPause = true;
        if (mCall != null)
            mCall.onPause();
    }

    public void resume() {
        mPause = false;
        if (mCall != null)
            mCall.onResume();
    }

    public void cancel() {
        cancel = true;
        mPause = false;
        if (mCall != null)
            mCall.onCancel();
    }


    private interface Api {
        @Streaming
        @GET()
        Observable<ResponseBody> download(@Url() String url);
    }

    OnDownloadListener mOnDownloadListener = new OnDownloadListener() {
        @Override
        public void onProgress(long bytesRead, long contentLength, boolean done) {
            while (mPause) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (cancel) {
                throw new ResponseCodeError("cancel");
            }
            mHandler.obtainMessage(KEY_SIZE, (int) bytesRead, (int) contentLength).sendToTarget();
        }
    };
}
