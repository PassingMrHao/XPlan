package com.module.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 *  Utils 工具类初始化相关 主要是为了避免其他util直接使用Application.this造成耦合太大
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static boolean isDebug;
    public static String HTTP_HOST = "";

    public static Utils instance = new Utils();

    private Utils() {}

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static Utils init(@NonNull final Context context,boolean isDebug) {
        Utils.context = context.getApplicationContext();
        Utils.isDebug = isDebug;
        return instance;
    }

    public static Utils setBaseUrl(@NonNull String baseUrl) {
        Utils.HTTP_HOST = baseUrl;
        return instance;
    }


    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }
}