package com.god.retrofit.util;

import android.content.Context;

/**
 * Created by abook23 on 2016/11/18.
 * Versions 1.0
 */

public class AppUtils {
    private static Context APP_CONTEXT;

    public static void initial(Context applicationContext) {
        APP_CONTEXT = applicationContext;
    }

    public static Context getApplicationContext() {
        if (APP_CONTEXT == null)
            throw new NullPointerException("not initial");
        return APP_CONTEXT;
    }
}
