package com.example.app.bean;

import android.support.annotation.ColorRes;

public class GridModel {
    private String title;
    private String mdName;
    private Class<?> mActivityClass;
    @ColorRes
    private int code;

    public GridModel(String title, Class activityClass) {
        this.title = title;
        this.mActivityClass = activityClass;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMdName(String mdName) {
        this.mdName = mdName;
    }

    public void setActivityClass(Class<?> activityClass) {
        mActivityClass = activityClass;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Class<?> getActivityClass() {
        return mActivityClass;
    }

    public String getTitle() {
        return title;
    }

    public String getMdName() {
        return mdName;
    }

    public int getCode() {
        return code;
    }
}
