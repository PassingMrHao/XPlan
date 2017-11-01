package com.module.common.net.download;

import android.os.AsyncTask;

import com.module.common.net.RetrofitCreator;
import com.module.common.net.callback.IRequestCallback;

import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 下载文件
 */
public final class DownloadHandler {

    private final String URL;
    private static final WeakHashMap<String, Object> PARAMS = new WeakHashMap();
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;
    private final IRequestCallback iRequestCallback;

    public DownloadHandler(String url,
                           Map<String, Object> params,
                           String downDir,
                           String extension,
                           String name,
                           IRequestCallback iRequestCallback) {
        this.URL = url;
        this.PARAMS.putAll(params);
        this.DOWNLOAD_DIR = downDir;
        this.EXTENSION = extension;
        this.NAME = name;
        this.iRequestCallback = iRequestCallback;
    }

    public final void handleDownload() {
        if (iRequestCallback != null) {
            iRequestCallback.onStart();
        }

        RetrofitCreator
                .getApiService()
                .download(URL, PARAMS)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            final ResponseBody responseBody = response.body();
                            final SaveFileTask task = new SaveFileTask(iRequestCallback);
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                    DOWNLOAD_DIR, EXTENSION, responseBody, NAME);

                            //这里一定要注意判断，否则文件下载不全
                            if (task.isCancelled()) {
                                if (iRequestCallback != null) {
                                    iRequestCallback.onComplete();
                                }
                            }
                        } else {
                            if (iRequestCallback != null) {
                                iRequestCallback.onError(response.code(), response.message());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (iRequestCallback != null) {
                            iRequestCallback.onError(-1, t.toString());
                        }
                    }
                });
    }
}
