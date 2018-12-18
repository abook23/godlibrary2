package com.god.mediastore.activity;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.god.mediastore.R;
import com.god.mediastore.base.BaseFragmentActivity;
import com.god.mediastore.fagment.FragmentGallery;
import com.god.mediastore.fagment.FragmentImage;
import com.god.mediastore.listener.OnItemViewListener;
import com.god.mediastore.util.CustPagerTransformer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by abook23 on 2015/10/21.
 * <p>
 * 2016年10月28日 10:35:57  v2.0
 */
public class ImageInfoActivity extends BaseFragmentActivity {

    public final static String PATHS = "paths";
    public final static String POSITION = "position";
    public final static String CHECK_MAX = "check_max";
    public final static String CHECK_COUNT = "check_count";
    public final static String CHECK_PATHS = "check_paths";
    public final static String SHOW_CHECKBOX = "showCheckBox";
    public final static String PREVIEW = "preview";

    public static String DATA = "data";//返回的数据

    private ArrayList<String> paths;
    private ArrayList<String> paths_check;
    private ViewPager viewPager;
    private FragmentGallery fragmentGallery;
    private List<FragmentImage> fragments = new ArrayList<>();
    private int check_max;
    private int check_count;
    private CheckBox checkBox;
    private int position;
    private TextView dataTv;
    private TextView timeTv;

    public static void start(Context context, String url) {
        ArrayList<String> paths = new ArrayList<>();
        paths.add(url);
        start(context, 0, paths);
    }

    /**
     * 图片预览 查看
     *
     * @param context
     * @param urls
     */
    public static void start(Context context, int position, ArrayList<String> urls) {
        Intent intent = new Intent(context, ImageInfoActivity.class);
        intent.putExtra(ImageInfoActivity.POSITION, position);
        intent.putStringArrayListExtra(ImageInfoActivity.PATHS, urls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_image_info);
        //  addTitleBarFragment();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        initView();
        initData();
        initListener();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.gb_photo_viewpager);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        dataTv = (TextView) findViewById(R.id.text1);
        timeTv = (TextView) findViewById(R.id.text2);
    }

    private void initData() {
        Intent intent = getIntent();
        check_max = intent.getIntExtra(CHECK_MAX, 5);
        check_count = intent.getIntExtra(CHECK_COUNT, 0);
        boolean showCheckBox = intent.getBooleanExtra(SHOW_CHECKBOX, false);
        boolean preview = intent.getBooleanExtra(PREVIEW, true);
        paths_check = intent.getStringArrayListExtra(CHECK_PATHS);
        paths = intent.getStringArrayListExtra(PATHS);
        position = getIntent().getIntExtra(POSITION, 0);//显示页

        if (preview)
            if (paths_check == null) {
                paths_check = new ArrayList<>();
                paths_check.addAll(paths);
            }
        if (paths == null) {
            paths = new ArrayList<>();
        }
        if (paths_check == null)
            paths_check = new ArrayList<>();

        if (!showCheckBox)
            checkBox.setVisibility(View.GONE);

        viewPager.setPageTransformer(true, new CustPagerTransformer(this));
        // 2. viewPager添加adapter
        for (int i = 0; i < paths.size(); i++) {
            // 预先准备10个fragment
            fragments.add(FragmentImage.newInstance());
        }
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                String path = paths.get(position);
                FragmentImage fragment = fragments.get(position);
                fragment.bindData(path);
                fragment.setOnFragmentImageListener(new FragmentImage.OnFragmentImageListener() {
                    @Override
                    public void onUserVisibleHint(boolean isVisibleToUser) {
                        if (isVisibleToUser) {
                            int position = viewPager.getCurrentItem();
                            String path = paths.get(position);
                            checkBox.setChecked(paths_check.indexOf(path) != -1);
                            fragmentGallery.setPosition(paths_check.indexOf(path));

                            try {
                                ExifInterface exif = new ExifInterface(path);
                                String dateStr = exif.getAttribute(ExifInterface.TAG_DATETIME);
                                if (dateStr != null) {
                                    Date date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).parse(dateStr);
                                    dataTv.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(date));
                                    timeTv.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
                                } else {
                                    dataTv.setText(null);
                                    timeTv.setText(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return fragment;
            }

            @Override
            public int getCount() {
                return paths.size();
            }
        });
        viewPager.setCurrentItem(position);
        fragmentGallery = FragmentGallery.newInstance(false);
        getSupportFragmentManager().beginTransaction().add(R.id.gb_fl_gallery, fragmentGallery).commit();
        fragmentGallery.setImageViewWithe(35);
        fragmentGallery.setPosition(position);
        fragmentGallery.setData(paths_check);
    }

    private void initListener() {
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewPager.getCurrentItem();
                String path = paths.get(position);
                if (checkBox.isChecked()) {
                    if (check_count >= check_max) {
                        checkBox.setChecked(false);
                    } else {
                        paths_check.add(path);
                        check_count++;
                    }
                } else {
                    paths_check.remove(path);
                    check_count--;
                }
                fragmentGallery.setData(paths_check);
                fragmentGallery.setPosition(paths_check.indexOf(path));
            }
        });
        fragmentGallery.setOnClickListener(new OnItemViewListener() {
            @Override
            public void onItemViewClick(View parentView, View childView, int position) {
                if (childView instanceof ImageView) {
                    int index = paths.indexOf(paths_check.get(position));
                    viewPager.setCurrentItem(index);
                    fragmentGallery.setPosition(position);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(DATA, paths_check);
            setResult(RESULT_OK, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onClickBack(View view) {
        finish();
    }
}
