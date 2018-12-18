godlibrary-retrofit
======================
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


# 简介
1. 基于 retrofit2 的网络请求封装 经过项目 实战 验证。

# 使用方法

## 引用
```java
compile 'com.abook23:godlibrary-retrofit:1.1.0'
```

## 初始化

在 Application 中初始化
```java
ApiService.init(getApplicationContext(), "http://172.16.0.22:8099");//
FileService.init(getApplicationContext(), "http://172.16.0.200:8080");//文件下载上传 比如 文件服务器 和项目部在同一服务器
```

## 网络请求
完整实例 在后面
```java
ApiService.create(UserApi.class).userInfo()
                .compose(RxJavaUtils.<Response<UserInfo>>defaultSchedulers())
                // 等于 .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebObserver<Response<UserInfo>>() {
                    @Override
                    protected void onSuccess(Response<UserInfo> userInfoResponse) {
                        Toast.makeText(getApplicationContext(), "请求成功" + userInfoResponse.getState(),
                         Toast.LENGTH_SHORT).show();
                    }
                });
```

## 上传文件（支持多文件上传）
```java
        //需要在 application 中初始化 FileService
        //FileService.init(getApplicationContext(),String baseUrl);
        String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
        MultipartBody multipartBody = MultipartUtils.filesToMultipartBody(new File(path));//
        FileService.getInit().create(FileApi.class, new OnUpLoadingListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                //根据业务要求,是否需要添加下载监听
                uploadUI(bytesRead, contentLength);
            }
        }).uploading("groupline/fileUpload/uploadFiles", multipartBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebObserver<List<UploadMsgBean>>() {
                    @Override
                    protected void onSuccess(List<UploadMsgBean> uploadMsgBeen) {
                        Toast.makeText(context, new Gson().toJson(uploadMsgBeen), Toast.LENGTH_SHORT).show();
                    }
                });
```

## 文件下载（支持大文件下载）
```java

        String url = "uploadFiles/apk/jdk-8u101-windows-x64.exe";
        final String fieName = url.substring(url.lastIndexOf("/") + 1);
        final String parentStr = FileUtils.getDiskCacheDir(getApplicationContext());
        FileService.getInit().create(FileApi.class, new OnDownloadListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                downloadUI(bytesRead, contentLength);
            }
        }).download(url)
                .map(new Func1<ResponseBody, File>() {
                    @Override
                    public File call(ResponseBody responseBody) {
                        return FileUtils.saveFile(responseBody.byteStream(), parentStr, fieName);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebObserver<File>() {
                    @Override
                    protected void onSuccess(File file) {
                        Toast.makeText(context, file.getPath(), Toast.LENGTH_SHORT).show();
                    }
                });
```

# 可以 暂停 取消

## 文件上传
```java
    private void uploadFileJD() {
        String url = "groupline/fileUpload/uploadFiles";
        String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
        uploadFile = new UploadFile(url, new File(path));
        uploadFile.setOnListener(new Call() {
            @Override
            public void onStart() {
            }

            @Override
            public void onPause() {
            }

            @Override
            public void onResume() {
            }

            @Override
            public void onSize(float size, float maxSize) {
            }

            @Override
            public void onFail(Throwable e) {
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {

            }

            @Override
            public void onCancel() {
            }
        });
    }
```

## 文件下载
```java
private void downloadFIleJD() {
        //http://gdown.baidu.com/data/wisegame/30d88b11f5745157/baidushoujizhushou_16792523.apk
        //uploadFiles/apk/jdk-8u101-windows-x64.exe
        downloadFile = new DownloadFile("uploadFiles/apk/jdk-8u101-windows-x64.exe", new com.god.retrofit.listener.download.Call() {
            @Override
            public void onStart() {
                mButDSuspend.setText("暂停");
            }

            @Override
            public void onPause() {
                mButDSuspend.setText("继续");
            }

            @Override
            public void onResume() {
                mButDSuspend.setText("暂停");
            }

            @Override
            public void onSize(float size, float maxSize) {
            }

            @Override
            public void onFail(Throwable e) {
            }

            @Override
            public void onSuccess(File file) {
            }

            @Override
            public void onCancel() {
            }
        });
    }
```


## 网络请求 实例
```java

/**
 * Created by abook23 on 2016-8-30.
 * E-mail abook23@163.com
 */
public interface UserApi {

    @POST(ServiceURL.login)
    @FormUrlEncoded
    Observable<RootBean<UserInfo>> login(
            @Field("userName") String userName,
            @Field("password") String password
    );
}


public interface UserService {
    /**
     * 登录
     *
     * @param userName 用户名
     * @param password 密码
     * @param call     回调
     */
    void login(String userName, String password, Call<RootBean> call);
}

public abstract class Call<T> implements ApiCall<T> {
    @Override
    public void onError(Throwable e) {

    }
}

public interface ApiCall<T> {
    void onError(Throwable e);
    void onSuccess(T t);
}


public class UserServiceImpl implements UserService {

    private UserApi userApi = ApiService.create(UserApi.class);

    @Override
    public void login(final String userName, final String password, final Call<RootBean> call) {
        userApi.login(userName, password).map(new Func1<RootBean<UserInfo>, RootBean<UserInfo>>() {
            @Override
            public RootBean<UserInfo> call(RootBean<UserInfo> userInfoRootBean) {
                if (userInfoRootBean.isSucceed()) {
                    UserInfo localUserInfo = userInfoRootBean.getContent();
                    // code ------ map 为 异步逻辑
                }
                return userInfoRootBean;
            }
        }).compose(RxJavaUtils.<RootBean<UserInfo>>defaultSchedulers()).subscribe(new WebObserver<RootBean<UserInfo>>() {
            @Override
            protected void onSuccess(RootBean<UserInfo> userInfoRootBean) {
                call.onSuccess(userInfoRootBean);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                call.onError(e);
            }
        });
    }

public abstract class WebObserver<T> extends ObserverBaseWeb<T> {

        @Override
        public void onNext(T t) {
            if (t instanceof RootBean) {
                RootBean response = (RootBean) t;
                if (response.getState() == -1) {//未登陆 或者登陆超时
                //code ---
                    return;
                }
            }
            onSuccess(t);
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
        }
        protected abstract void onSuccess(T t);
}

public class LoginActivity extends Activity {
        private void LoginClick() {
            userService.login(loginName, pwd, new Call<RootBean>() {
                @Override
                public void onSuccess(RootBean rootBean) {
                    Toast.makeText(context, rootBean.getMsg(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                }
            });
        }
}

```
## 登录超时, Token 过期 TokenInterceptor
```java
    private void initHttp() {
        L.d("初始化-->initHttp");
        ApiService.DEBUG = true;
        //ApiService.logModel = LoggingInterceptor.LogModel.all;
        FileService.init(getApplicationContext(), ServiceURL.BASE_File_URL);
        ApiService apiService = ApiService.init(getApplicationContext(), ServiceURL.BASE_URL);
        //检验登录是否超时 toke是否过期
        apiService.addInterceptor(new TokenInterceptor<RootBean<UserInfo>>() {
            @Override
            protected boolean onAuthenticator(Response response) {
                //返回true 则执行 onAfresh()
                return response.code()==401;//401 需要重新登录
            }

            @Override
            protected Call<RootBean<UserInfo>> onAfresh() {//重新登录接口
                String userId = PreferenceUtils.getParam(SpfKey.userId);
                UserDao userDao = BaseApplication.getDaoSession().getUserDao();
                User user = userDao.load(userId);
                return ApiService.create(UserApi.class).loginAfresh(user.getName(), user.getPassword());
            }

            @Override
            protected Request onNewRequest(Request OldRequest, RootBean<UserInfo> userInfoRootBean) {
                //验证登录成功
                //可以修改再次访问的接口内容,不然 token
                return null;
            }
        });
    }
```

# 项目中的 dependencies

```java
dependencies {
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'io.reactivex:rxjava:1.2.2'
    compile 'io.reactivex:rxandroid:1.2.1'
    compile 'com.squareup.retrofit2:retrofit:2.1.0'
    compile 'com.squareup.retrofit2:adapter-rxjava:2.1.0'
    compile 'com.squareup.retrofit2:converter-gson:2.1.0'
}
```

License
-------

    Copyright 2017 Wasabeef

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.