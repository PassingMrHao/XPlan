package com.module.common.net;

import com.module.common.net.callback.IRequestCallback;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 添加网络请求参数 Builder模式,链式函数调用
 */
public final class HttpManagerBuilder {

    private String mUrl = null;
    private WeakHashMap<String, Object> mParams = new WeakHashMap();
    private RequestBody mBody = null;
    private File mFile = null;
    private String mDownloadDir = null;
    private String mExtension = null;
    private String mName = null;
    private IRequestCallback mIRequestCallback = null;

    HttpManagerBuilder() {
    }

    public final HttpManagerBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final HttpManagerBuilder params(WeakHashMap<String, Object> params) {
        mParams.putAll(params);
        return this;
    }

    public final HttpManagerBuilder params(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public final HttpManagerBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final HttpManagerBuilder file(String file) {
        this.mFile = new File(file);
        return this;
    }

    public final HttpManagerBuilder name(String name) {
        this.mName = name;
        return this;
    }

    public final HttpManagerBuilder dir(String dir) {
        this.mDownloadDir = dir;
        return this;
    }

    public final HttpManagerBuilder extension(String extension) {
        this.mExtension = extension;
        return this;
    }

    public final HttpManagerBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), raw);
        return this;
    }

    public final HttpManagerBuilder requestCallbak(IRequestCallback iRequestCallback) {
        this.mIRequestCallback = iRequestCallback;
        return this;
    }

    public final HttpManager build() {
        return new HttpManager(mUrl, mParams,
                mDownloadDir, mExtension, mName,
                mBody, mFile,
                mIRequestCallback);
    }
}
