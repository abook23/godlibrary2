package com.god.retrofit.rxjava;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by abook23 on 2016/11/18.
 * Versions 1.0
 */

public class RxJavaUtils {

    /**
     * 请求在io 线程
     * 响应在 UI线程
     * observable.compose(RxJavaUtils.defaultSchedulers())
     */
    public static <T> Observable.Transformer<T, T> defaultSchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
