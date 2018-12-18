package com.god.util;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by 杨雄 on 2017/9/4.
 */

public class DataManager {
    LruCache<String, Bitmap> mLruCacheBitmap = new LruCache<>(20);
    LruCache<String, Object> mLruCacheObject = new LruCache<>(50);
    static DataManager sDataManger;

    public static DataManager getInstance() {
        if (sDataManger == null) {
            sDataManger = new DataManager();
        }
        return sDataManger;
    }

    public <T> T get(String key) {
        final Object v = mLruCacheObject.get(key);
        mLruCacheObject.remove(key);
        return (T) v;
    }

    public DataManager put(String key, Object o) {
        if (key == null || o == null)
            return this;
        mLruCacheObject.put(key, o);
        return this;
    }

    public Bitmap getBitmap(String key) {
        final Bitmap b = mLruCacheBitmap.get(key);
        mLruCacheBitmap.remove(key);
        return b;
    }

    public DataManager putBitmap(String key, Bitmap bitmap) {
        mLruCacheBitmap.put(key, bitmap);
        return this;
    }

    public void clearBitmap(String key) {
        mLruCacheBitmap.remove(key);
    }

    public void clearBitmap() {
        if (mLruCacheBitmap.size() > 0)
            mLruCacheBitmap.evictAll();
    }
}
