package com.example.app.activity.retrofit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.activity.retrofit.api.FileApi;
import com.example.app.activity.retrofit.api.WebObserver;
import com.example.app.bean.UploadMsgBean;
import com.god.retrofit.DownloadFile;
import com.god.retrofit.FileService;
import com.god.retrofit.UploadFile;
import com.god.retrofit.listener.loading.Call;
import com.god.retrofit.progress.OnDownloadListener;
import com.god.retrofit.progress.OnUpLoadingListener;
import com.god.retrofit.util.FileUtils;
import com.god.retrofit.util.MultipartUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FileActivity extends AppCompatActivity {

    @BindView(R.id.textView3)
    TextView mTextView3;
    @BindView(R.id.button5)
    Button mButton5;
    @BindView(R.id.button6)
    Button mButton6;
    @BindView(R.id.progressBar3)
    ProgressBar mProgressBar3;
    @BindView(R.id.but_u_start)
    Button mButUStart;
    @BindView(R.id.but_u_suspend)
    Button mButUSuspend;
    @BindView(R.id.but_u_cancel)
    Button mButUCancel;
    @BindView(R.id.progressBar4)
    ProgressBar mProgressBar4;
    @BindView(R.id.but_d_start)
    Button mButDStart;
    @BindView(R.id.but_d_suspend)
    Button mButDSuspend;
    @BindView(R.id.but_d_cancel)
    Button mButDCancel;
    private Context context;
    private UploadFile uploadFile;
    private DownloadFile downloadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        ButterKnife.bind(this);
        context = this;

    }

    @OnClick({R.id.button5, R.id.button6, R.id.but_u_start, R.id.but_u_suspend, R.id.but_u_cancel, R.id.but_d_start, R.id.but_d_suspend, R.id.but_d_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button5:
                uploadFile1();
                break;
            case R.id.button6:
                downloadFIle();
                break;
            case R.id.but_u_start:
                uploadFileJD();
                break;
            case R.id.but_u_suspend:
                if (uploadFile.isPause()) {
                    uploadFile.resume();
                } else {
                    uploadFile.pause();
                }
                break;
            case R.id.but_u_cancel:
                uploadFile.cancel();
                break;
            case R.id.but_d_start:
                downloadFIleJD();
                break;
            case R.id.but_d_suspend:
                if (downloadFile.isPause()) {
                    downloadFile.resume();
                } else {
                    downloadFile.pause();
                }
                break;
            case R.id.but_d_cancel:
                downloadFile.cancel();
                break;
        }
    }

    private void downloadFIleJD() {
        //http://gdown.baidu.com/data/wisegame/30d88b11f5745157/baidushoujizhushou_16792523.apk
        //uploadFiles/apk/jdk-8u101-windows-x64.exe
        downloadFile = new DownloadFile("uploadFiles/apk/jdk-8u101-windows-x64.exe", new com.god.retrofit.listener.download.Call() {
            @Override
            public void onStart() {
                mButDSuspend.setText("暂停");
                mButDCancel.setEnabled(true);
                mButDStart.setEnabled(false);
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
                mProgressBar4.setMax((int) maxSize);
                mProgressBar4.setProgress((int) size);
            }

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onSuccess(File file) {
                mButDStart.setText("开始");
                //mButDCancel.setEnabled(false);
                Toast.makeText(context, file.getPath()+"", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                mProgressBar4.setProgress(0);
                mButDStart.setText("开始");
                mButDCancel.setEnabled(false);
                mButDStart.setEnabled(true);
            }
        });
    }

    private void uploadFileJD() {
        String url = "groupline/fileUpload/uploadFiles";
        String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
        uploadFile = new UploadFile(url, new File(path));
        uploadFile.setOnListener(new Call() {
            @Override
            public void onStart() {
                mButUSuspend.setText("暂停");
                mButUStart.setEnabled(false);
            }

            @Override
            public void onPause() {
                mButUSuspend.setText("继续");
            }

            @Override
            public void onResume() {
                mButUSuspend.setText("暂停");
            }

            @Override
            public void onSize(float size, float maxSize) {
                mProgressBar3.setMax((int) maxSize);
                mProgressBar3.setProgress((int) size);
            }

            @Override
            public void onFail(Throwable e) {
                Toast.makeText(FileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                mButUStart.setEnabled(true);
                mButUCancel.setEnabled(false);
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {

            }

            @Override
            public void onCancel() {
                mProgressBar3.setProgress(0);
                mButUStart.setEnabled(true);
                mButUCancel.setEnabled(false);
                mButUStart.setText("开始");
            }
        });
    }

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
    }

    /**
     * 如果下载地址是相对地址 则需在 需要在 application 中初始化 FileService.init()
     * 如果是绝对地址可以直接下载
     * 更多参考 请参考 okhttp或者 ritrofit2
     */
    private void uploadFile1() {

        //需要在 application 中初始化 FileService
        //FileService.init(getApplicationContext(),String baseUrl);
        String path = FileUtils.getDowloadDir(getApplication()) + "/jdk-8u101-windows-x64.exe";
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

    private void uploadUI(final long bytesRead, final long contentLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton5.setText(String.format(Locale.getDefault(), "%.2f", (float) bytesRead / contentLength * 100));
            }
        });
    }

    private void downloadUI(final long bytesRead, final long contentLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton6.setText(String.format(Locale.getDefault(), "%.2f", (float) bytesRead / contentLength * 100));
            }
        });
    }
}
