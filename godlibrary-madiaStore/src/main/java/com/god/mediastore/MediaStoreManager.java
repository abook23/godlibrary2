package com.god.mediastore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.god.mediastore.activity.CameraVideoActivity;
import com.god.mediastore.activity.ImageInfoActivity;
import com.god.mediastore.activity.PhotoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abook23 on 2017/11/7.
 */

public class MediaStoreManager {

    /**
     * 图库
     *
     * @param ac
     * @param checkMax
     * @param checkPath
     * @param resultCode
     */
    public static void startMediaStoreImagesForResult(Activity ac, int checkMax, ArrayList<String> checkPath, int resultCode) {
        PhotoActivity.startActivityForResult(ac, checkMax, checkPath, resultCode);
    }

    /**
     * 打开相机
     *
     * @param activity
     * @param requestCode
     */
    public static void startCameraForResult(Activity activity, int requestCode) {
        CameraVideoActivity.startForResult(activity, 1, requestCode);
    }

    /**
     * 打开相机
     *
     * @param activity
     * @param checkMax
     * @param requestCode
     */
    public static void startCameraForResult(Activity activity, int checkMax, int requestCode) {
        CameraVideoActivity.startForResult(activity, checkMax, requestCode);
    }

    /**
     * 图片浏览
     *
     * @param context
     * @param url
     */
    public static void startGallery(Context context, String url) {
        ArrayList<String> list = new ArrayList<>();
        list.add(url);
        startGallery(context, 0, list);
    }

    /**
     * 图片浏览
     *
     * @param context
     * @param showUrlPosition
     * @param urls
     */
    public static void startGallery(Context context, int showUrlPosition, List<String> urls) {
        ArrayList<String> list = new ArrayList<>();
        if (urls != null)
            list.addAll(urls);
        Intent intent = new Intent(context, ImageInfoActivity.class);
        intent.putExtra(ImageInfoActivity.POSITION, showUrlPosition);
        intent.putExtra(ImageInfoActivity.SHOW_CHECKBOX, false);
        intent.putStringArrayListExtra(ImageInfoActivity.PATHS, list);
        context.startActivity(intent);
    }
}
