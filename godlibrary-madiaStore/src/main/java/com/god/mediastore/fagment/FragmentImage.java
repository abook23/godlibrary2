package com.god.mediastore.fagment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.god.mediastore.R;
import com.god.mediastore.widget.PinchImageView;

/**
 * Created by abook23 on 2016/10/25.
 */

public class FragmentImage extends Fragment {
    private String path;
    private OnFragmentImageListener mListener;
    private PinchImageView mImageView;
    public int height, width;
    public FragmentImage() {
    }

    public static FragmentImage newInstance() {
        FragmentImage fragment = new FragmentImage();
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mListener != null) {
            mListener.onUserVisibleHint(isVisibleToUser);
        }
    }

    public void setOnFragmentImageListener(OnFragmentImageListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment_image, container, false);
        mImageView = (PinchImageView) view.findViewById(R.id.imageView1);
        if (height == 0) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            height = displayMetrics.heightPixels;
            width = displayMetrics.widthPixels;
        }

        //Glide.with(this).load(path).diskCacheStrategy(DiskCacheStrategy.ALL).override(width / 2, height / 2).into(mImageView);
        Glide.with(this).load(path).into(mImageView);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    public void bindData(String path) {
        this.path = path;
    }

    public interface OnFragmentImageListener {
        void onUserVisibleHint(boolean isVisibleToUser);
    }
}
