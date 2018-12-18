## Retrofit Upload

###初始化项目
```java
//需要在 application 中初始化 FileService
FileService.init(getApplicationContext(), "http://172.16.0.200:8080");

```
### 创建API
```java
public interface FileApi {
    @POST()
    Observable<List<UploadMsgBean>> uploading(@Url String url, @Body MultipartBody multipartBody);

    @Streaming
    @GET()
    Observable<ResponseBody> download(@Url() String url);
}
```
## 普通功能
### 普通上传(支持多文件)
```java
/**
 * 如果下载地址是相对地址 则需在 需要在 application 中初始化 FileService.init()
 * 如果是绝对地址可以直接下载
 * 更多参考 请参考 okhttp或者 ritrofit2
 */
private void uploadFile1() {
    //FileService.init(getApplicationContext(),String baseUrl);
    String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
    //支持多文件上传
    MultipartBody multipartBody = MultipartUtils.filesToMultipartBody(new File(path));
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
}
```
### 普通下载
```java
/**
 * 如果下载地址是相对地址 则需在 需要在 application 中初始化 FileService.init()
 * 如果是绝对地址可以直接下载
 * 更多参考 请参考 okhttp或者 ritrofit2
 */
private void downloadFIle() {
    //需要在 application 中初始化 FileService
    //FileService.init(getApplicationContext(),String baseUrl);
    String url = "uploadFiles/apk/jdk-8u101-windows-x64.exe";
    final String fieName = url.substring(url.lastIndexOf("/") + 1);
    final String parentStr = FileUtils.getDiskCacheDir(getApplicationContext());
    FileService.getInit().create(FileApi.class, new OnDownloadListener() {
        @Override
        public void onProgress(long bytesRead, long contentLength, boolean done) {
            downloadUI(bytesRead, contentLength);
        }
    }).download(url).map(new Func1<ResponseBody, File>() {
        @Override
        public File call(ResponseBody responseBody) {
            return FileUtils.saveFile(responseBody.byteStream(), parentStr, fieName);
        }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new WebObserver<File>() {
        @Override
        protected void onSuccess(File file) {
            Toast.makeText(context, file.getPath(), Toast.LENGTH_SHORT).show();
        }
    });
}
```
### 浏览器下载
```java
AndroidUtils.downl(this, url);//浏览器下载
```
## 高级功能

### 上传监听
```java
private void uploadFileJD() {
    String url = "groupline/fileUpload/uploadFiles";
    String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
    uploadFile = new UploadFile(url, new File(path));
    uploadFile.setOnListener(new com.god.retrofit.listener.loading.Call() {
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
    uploadFile.start();
}
```
### 下载监听
```java
private void downloadFIleJD() {
    //uploadFiles/apk/jdk-8u101-windows-x64.exe
    //http://sw.bos.baidu.com/sw-search-sp/software/d4e97ccd4bd9f/jdk-8u144-windows-i586_8.0.1440.1.exe
    downloadFile = new DownloadFile("url");
    downloadFile.setCall(new com.god.retrofit.listener.download.Call() {
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
        public void onSuccess(File file) {
        }

        @Override
        public void onCancel() {
        }
    });
    downloadFile.start();
}
```


