
package com.god.mediastore.fagment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.god.mediastore.R;
import com.god.mediastore.util.AutoFocusManager;
import com.god.mediastore.util.PermissionUtil;
import com.god.mediastore.widget.VideoProgress;
import com.sprylab.android.widget.TextureVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CameraVideoFragment extends Fragment implements Callback, OnClickListener {
    private static String[] PERMISSIONS_CAMERA = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int REQUEST_CONTACTS = 1;
    private static String TAG = CameraVideoFragment.class.getSimpleName();
    private String PHOTO_PATH;
    private String VIDEO_PATH;
    ImageView cameraTransform;
    private Context context;
    ImageView iVvHd;
    private boolean isPlayVideo;
    private Camera mCamera;
    ImageView mCameraBack;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    ImageView mCameraYes;
    private CountDownTimer mCountDownTimer;
    private Definition mDefinition;
    private DirectionOrientationListener mDirectionOrientationListener;
    private OnCameraVideoListener mListener;
    private OnCameraVideoTouchListener mTouchListener;
    private int mOrientation;
    private SurfaceHolder mSurfaceHolder;
    TextView mTvSecond;
    private int mVideoMaxDuration = 10000;//默认录制时间长度
    private long mVideoMaxZie = 50 * 1024 * 1024;//默认录制视频大小
    VideoProgress mVideoProgress;
    private float mVideoRatio;
    TextureVideoView mVideoView;
    private int pictureSizeHeight;
    private int pictureSizeWidth;

    private List<Camera.Size> pictureSizes;
    private List<Camera.Size> previewSizes;
    private Camera.Parameters parameters;

    private MediaRecorder recorder;
    SurfaceView surfaceView;
    private AutoFocusManager autoFocusManager;//自动对焦 zxing 里面的
    private int widthDisplay, heightDisplay;
    private boolean touchPointAutoFocus;

    public static CameraVideoFragment newInstance() {
        CameraVideoFragment cameraVideoFragment = new CameraVideoFragment();
        cameraVideoFragment.setArguments(new Bundle());
        return cameraVideoFragment;
    }

    public static CameraVideoFragment newInstance(boolean touchPointAutoFocus) {
        CameraVideoFragment cameraVideoFragment = new CameraVideoFragment();
        cameraVideoFragment.setArguments(new Bundle());
        cameraVideoFragment.touchPointAutoFocus = touchPointAutoFocus;
        return cameraVideoFragment;
    }


    public void setOnCameraVideoListener(OnCameraVideoListener listener) {
        mListener = listener;
    }

    public void setOnCameraVideoTouchListener(OnCameraVideoTouchListener listener) {
        mTouchListener = listener;
    }


    public CameraVideoFragment() {
        mDefinition = Definition.SD;
        mCameraId = 0;
        mOrientation = 0;
        mVideoRatio = 1;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_video, viewGroup, false);
        initView(view);
        initDisplayMetrics();
        context = getContext();
        mVideoProgress.setOnCameraVideoListener(new ClickListener());
        if (mDefinition == Definition.HD) {
            iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd);
        } else {
            iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd_off);
        }

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(this);
        initCountDownTimer();
        mDirectionOrientationListener = new DirectionOrientationListener(context, 3);
        if (mDirectionOrientationListener.canDetectOrientation()) {
            mDirectionOrientationListener.enable();
            return view;
        } else {
            Log.d("chengcj1", "Can't Detect Orientation");
            return view;
        }
    }

    private void initView(View view) {
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView_video);
        mVideoProgress = (VideoProgress) view.findViewById(R.id.videoProgress);
        mTvSecond = (TextView) view.findViewById(R.id.tv_second);
        iVvHd = (ImageView) view.findViewById(R.id.iv_hd);
        mCameraYes = (ImageView) view.findViewById(R.id.camera_yes);
        mCameraBack = (ImageView) view.findViewById(R.id.camera_back);
        mVideoView = (TextureVideoView) view.findViewById(R.id.videoView);
        cameraTransform = (ImageView) view.findViewById(R.id.iv_camera_transform);
        mTvSecond.setOnClickListener(this);
        iVvHd.setOnClickListener(this);
        mCameraYes.setOnClickListener(this);
        mCameraBack.setOnClickListener(this);
        cameraTransform.setOnClickListener(this);

        if (touchPointAutoFocus) {
            surfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            doAutoFocus(motionEvent.getX(), motionEvent.getY());
                            break;
                    }
                    return true;
                }
            });
        } else {
            surfaceView.setOnClickListener(this);
        }

    }

    private void initDisplayMetrics() {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        widthDisplay = dm.widthPixels;
        heightDisplay = dm.heightPixels;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mSurfaceHolder = surfaceHolder;
        if (mCamera == null) {
            requestContactsPermissions();
        }
    }

    private void requestContactsPermissions() {
        if (PermissionUtil.requestPermission(this, PERMISSIONS_CAMERA, REQUEST_CONTACTS)) {
            openCamera(mCameraId);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 1) {
            if (!PermissionUtil.verifyPermissions(context, PERMISSIONS_CAMERA, results)) {
                Log.i(TAG, "缺少必要的权限");
                Toast.makeText(context, "缺少 相应权限", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            }
            Log.i(TAG, "已经全部授权");
            openCamera(mCameraId);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = null;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    /**
     * 打开相机
     *
     * @param cameraId 相机id
     */
    private void openCamera(int cameraId) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        mCamera = Camera.open(cameraId);
        mCamera.setDisplayOrientation(90);
        if (pictureSizeHeight > 0) {
            setParameters(pictureSizeWidth, pictureSizeHeight);
        } else if (mDefinition == Definition.SD) {
            setParameters(1280, 960);
        } else if (mDefinition == Definition.HD) {
            setParameters(-1, -1);
        }
        autoFocusManager = new AutoFocusManager(mCamera);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化录像机
     */
    private void initVideo() {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }

        if (mCamera == null) {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
        }

        mCamera.unlock();
        recorder.setCamera(mCamera);
        recorder.setAudioSource(1);
        recorder.setVideoSource(1);
        recorder.setOutputFormat(2);
        recorder.setVideoEncoder(2);
        recorder.setAudioEncoder(3);
        recorder.setVideoFrameRate(25);
        Size size;
        if (mDefinition == Definition.HD) {
            size = getDefaultSize(pictureSizes, 720);
            recorder.setVideoSize(size.width, size.height);
        } else {
            size = getDefaultSize(pictureSizes, 480);
            recorder.setVideoSize(size.width, size.height);
        }

        recorder.setMaxDuration(mVideoMaxDuration);
        recorder.setMaxFileSize(mVideoMaxZie);
        recorder.setVideoEncodingBitRate((int) (mVideoRatio * 1024.0F * 1024.0F));
        recorder.setAudioChannels(2);
        recorder.setAudioEncodingBitRate(128);
        mOrientation += 90;
        if (mOrientation > 270) {
            mOrientation = 0;
        }

        recorder.setOrientationHint(mOrientation);
        String var3 = System.currentTimeMillis() + ".mp4";
        VIDEO_PATH = getDiskDir(context, "");
        File file = new File(VIDEO_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(VIDEO_PATH, var3);
        recorder.setOutputFile(file.getAbsolutePath());
        recorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        VIDEO_PATH = file.getAbsolutePath();
    }


    /**
     * 播放视频
     */
    private void startVideo() {
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 聚焦
     */
    private void doAutoFocus(float x, float y) {
        try {
            mCamera.cancelAutoFocus();
            parameters = mCamera.getParameters();

            //聚焦点击位置
            if (touchPointAutoFocus && x > 0 && y > 0) {
                List<Camera.Area> areas = new ArrayList<>();
                List<Camera.Area> areasMetrix = new ArrayList<>();
                Size previewSize = parameters.getPreviewSize();
                Rect focusRect = calculateTapArea(x, y, 1.0f, previewSize);
                Rect metrixRect = calculateTapArea(x, y, 1.5f, previewSize);
                areas.add(new Camera.Area(focusRect, 1000));
                areasMetrix.add(new Camera.Area(metrixRect, 1000));
                parameters.setMeteringAreas(areasMetrix);
                parameters.setFocusAreas(areas);
            }

            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                        if (!Build.MODEL.equals("KORIDY H30")) {
                            parameters = camera.getParameters();
                            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                            camera.setParameters(parameters);
                        } else {
                            parameters = camera.getParameters();
                            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                            camera.setParameters(parameters);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY = (int) (x / widthDisplay * 2000 - 1000);
        int centerX = (int) (y / heightDisplay * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }


    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void delFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

    }

    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5F);
    }

    /**
     * 比例
     *
     * @param size
     * @param rate 16:9 1.777777
     * @return
     */
    private boolean equalRate(Size size, float rate) {
        return (double) Math.abs((float) size.width / (float) size.height - rate) <= 0.2D;
    }

    private Size getDefaultSize(List<Size> sizes, int height) {
        int i = 0;
        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++i) {
            Size size = (Size) var4.next();
            if (size.height >= height && equalRate(size, 1.77F)) {
                Log.i(TAG, "最终设置尺寸:w = " + size.width + "h = " + size.height);
                break;
            }
        }
        return i == sizes.size() ? sizes.get(i - 1) : sizes.get(i);
    }

    private String getDiskDir(Context context, String dir) {
        String path;
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && !Environment.isExternalStorageRemovable()) {
            path = context.getCacheDir().getPath();
        } else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return dir == null ? path : path + File.separator + dir;
    }

    private Size getPictureSize(List<Size> sizes, int height) {
        int i = 0;
        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++i) {
            Size var5 = (Size) var4.next();
            if (var5.height > height && equalRate(var5, 1.77F)) {
                Log.i(TAG, "最终设置图片尺寸:w = " + var5.width + "h = " + var5.height);
                break;
            }
        }
        return i == sizes.size() ? sizes.get(i - 1) : sizes.get(i);
    }

    private Size getPreviewSize(List<Size> sizes, int height) {
        int var3 = 0;

        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++var3) {
            Size var5 = (Size) var4.next();
            if (var5.height > height && equalRate(var5, 1.77F)) {
                Log.i(TAG, "最终设置预览尺寸:w = " + var5.width + "h = " + var5.height);
                break;
            }
        }

        return var3 == sizes.size() ? sizes.get(var3 - 1) : sizes.get(var3);
    }


    /**
     * 设置相机 图片大小
     *
     * @param width  宽
     * @param height 高
     */
    private void setParameters(int width, int height) {
        Parameters parameters = mCamera.getParameters();
        pictureSizes = parameters.getSupportedPictureSizes();
        previewSizes = parameters.getSupportedPreviewSizes();
        Collections.reverse(pictureSizes);
        Collections.reverse(previewSizes);
        Size size;
        if (height > 0) {
            size = getPictureSize(pictureSizes, height);
        } else {
            size = pictureSizes.get(pictureSizes.size() - 1);
        }

        parameters.setPictureSize(size.width, size.height);
        size = getPreviewSize(previewSizes, surfaceView.getWidth());
        parameters.setPreviewSize(size.width, size.height);

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

        mCamera.setParameters(parameters);
        mCamera.cancelAutoFocus();
    }

    /**
     * 定时器
     */
    private void initCountDownTimer() {
        mCountDownTimer = new CountDownTimer(mVideoMaxDuration, 100) {
            @Override
            public void onFinish() {
                mVideoProgress.setProgress(mVideoMaxDuration);
                mTvSecond.setText((mVideoMaxDuration / 1000) + "s");
            }

            @Override
            public void onTick(long timer) {
                mVideoProgress.setMax(mVideoMaxDuration);
                float var3 = mVideoMaxDuration - timer;
                mVideoProgress.setProgress(var3);
                mTvSecond.setText((int) var3 / 1000 + "s");
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDirectionOrientationListener.disable();
    }

    public void setDefinition(Definition var1) {
        mDefinition = var1;
    }


    public void setPictureSize(int width, int height) {
        pictureSizeWidth = width;
        pictureSizeHeight = height;
    }

    public void setVideoMaxDuration(int maxDuration) {
        mVideoMaxDuration = maxDuration;
    }

    public void setVideoMaxZie(long maxZie) {
        mVideoMaxZie = maxZie;
    }

    public void setVideoRatio(float videoRatio) {
        if (videoRatio > 5.0F) {
            videoRatio = 5.0F;
        }
        mVideoRatio = videoRatio;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.surfaceView_video) {
            if (mCamera != null) {
                doAutoFocus(0, 0);//使用 触摸监听了
            }
        } else if (viewId == R.id.iv_hd) {//高清
            if (mDefinition == Definition.SD) {
                mDefinition = Definition.HD;
                iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd);
            } else {
                mDefinition = Definition.SD;
                iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd_off);
            }
            openCamera(mCameraId);
        } else if (viewId == R.id.iv_camera_transform) {//前后摄像头切换
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            openCamera(mCameraId);
        } else if (viewId == R.id.camera_yes) {//选择当前拍摄
            if (mTouchListener != null) {
                mTouchListener.onSuccess(PHOTO_PATH);
            }
            if (mListener != null) {
                if (isPlayVideo) {
                    mListener.onFragmentResult(VIDEO_PATH, "mp4");
                    return;
                }
                mListener.onFragmentResult(PHOTO_PATH, "jpg");
            }
        } else if (viewId == R.id.camera_back) {//放弃拍摄
            startAnimator2();
            if (isPlayVideo) {
                stopVideo();
                delFile(VIDEO_PATH);
                return;
            }
            mCamera.startPreview();
            delFile(PHOTO_PATH);
            if (mTouchListener != null) {
                mTouchListener.onCancel();
            }
        }
    }

    private class ClickListener implements VideoProgress.OnClickListener {
        @Override
        public void onClick() {
            cameraTakePicture();
            if (mTouchListener != null)
                mTouchListener.onClick();
        }

        @Override
        public void onLongClick() {
            mCountDownTimer.start();
            initVideo();
            startVideo();
            if (mTouchListener != null)
                mTouchListener.onLongClick();
        }

        @Override
        public void onLongUpClick() {
            mTvSecond.setText("");
            mCountDownTimer.cancel();
            recorder.stop();
            recorder.reset();
            startAnimator1();
            playVideo();
        }
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        isPlayVideo = true;
        surfaceView.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
        cameraTransform.setVisibility(View.GONE);
        iVvHd.setVisibility(View.GONE);
        mVideoView.setVideoURI(Uri.parse(VIDEO_PATH));
        mVideoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoView.start();
        mVideoView.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    /**
     * 停止视频
     */
    private void stopVideo() {
        isPlayVideo = false;
        iVvHd.setVisibility(View.VISIBLE);
        cameraTransform.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.GONE);
        mVideoView.pause();
        mVideoView.stopPlayback();
    }

    /**
     * 保存照片
     */
    private void cameraTakePicture() {
        mCamera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        mOrientation = mOrientation + 90;
                        if (mOrientation > 270) {
                            mOrientation = 0;
                        }

                        matrix.preRotate((float) mOrientation);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        String fileName = System.currentTimeMillis() + ".jpg";
                        PHOTO_PATH = getDiskDir(context, "/DCIM/Camera");
                        File file = new File(PHOTO_PATH);
                        if (!file.exists()) {
                            file.mkdirs();
                        }

                        file = new File(PHOTO_PATH, fileName);
                        PHOTO_PATH = file.getAbsolutePath();
                        try {
                            FileOutputStream os = new FileOutputStream(file);
                            bitmap.compress(CompressFormat.JPEG, 100, os);
                            bitmap.recycle();
                            os.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.gc();
                        }
                    }
                }).start();
                startAnimator1();
            }
        });
    }

    //动画
    private void startAnimator1() {
        mTvSecond.setText("");
        mVideoProgress.setVisibility(View.GONE);
        mCameraYes.setVisibility(View.VISIBLE);
        mCameraBack.setVisibility(View.VISIBLE);
        float var1 = mCameraYes.getTranslationX();
        float var2 = mCameraYes.getTranslationX();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mCameraYes, "translationX", var1, (float) dp2px(context, 80.0F));
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mCameraBack, "translationX", var2, (float) (-dp2px(context, 80.0F)));
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mVideoProgress, "alpha", 1.0F, 0.0F);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mCameraYes, "alpha", 0.0F, 1.0F);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mCameraBack, "alpha", 0.0F, 1.0F);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator1, animator2, animator3, animator4, animator5);
        animatorSet.setDuration(300L);
        animatorSet.start();
    }

    private void startAnimator2() {
        mVideoProgress.setVisibility(View.VISIBLE);
        mCameraYes.setVisibility(View.GONE);
        mCameraBack.setVisibility(View.GONE);
        float var1 = mCameraYes.getTranslationX();
        float var2 = mCameraYes.getTranslationX();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mCameraYes, "translationX", var1, 0.0F);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mCameraBack, "translationX", -var2, 0.0F);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mVideoProgress, "alpha", 0.0F, 1.0F);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mCameraYes, "alpha", 1.0F, 0.0F);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mCameraBack, "alpha", 1.0F, 0.0F);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator1, animator2, animator3, animator4, animator5);
        animatorSet.setDuration(300L);
        animatorSet.start();
    }

    public enum Definition {
        HD, SD
    }

    /**
     * 重力感应
     */
    public class DirectionOrientationListener extends OrientationEventListener {

        public DirectionOrientationListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != -1) {
                orientation = (orientation + 45) / 90 * 90 % 360;
                if (orientation != mOrientation) {
                    mOrientation = orientation;
                    return;
                }
            }
        }
    }


    public interface OnCameraVideoListener {
        void onFragmentResult(String path, String type);
    }

    public interface OnCameraVideoTouchListener {
        void onLongClick();

        void onClick();

        void onSuccess(String path);

        void onCancel();
    }
}
