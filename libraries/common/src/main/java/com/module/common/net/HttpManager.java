package com.module.common.net;

import com.module.common.net.callback.IRequestCallback;
import com.module.common.net.callback.RetrofitCallback;
import com.module.common.net.download.DownloadHandler;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * 网络请求 统一管理
 */
public final class HttpManager {

    private final String URL;
    private final WeakHashMap<String, Object> PARAMS = new WeakHashMap();
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;
    private final RequestBody BODY;
    private final File FILE;
    private final IRequestCallback iRequestCallback;

    enum HttpMethod {
        GET,
        POST,
        POST_RAW,
        PUT,
        PUT_RAW,
        DELETE,
        UPLOAD
    }

    public static HttpManagerBuilder builder() {
        return new HttpManagerBuilder();
    }

    HttpManager(String url,
                Map<String, Object> params,
                String downloadDir,
                String extension,
                String name,
                RequestBody body,
                File file,
                IRequestCallback iRequestCallback) {
        this.URL = url;
        this.PARAMS.putAll(params);
        this.DOWNLOAD_DIR = downloadDir;
        this.EXTENSION = extension;
        this.NAME = name;
        this.BODY = body;
        this.FILE = file;
        this.iRequestCallback = iRequestCallback;
    }

    public final void get() {
        request(HttpMethod.GET);
    }

    public final void post() {
        if (BODY == null) {
            request(HttpMethod.POST);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.POST_RAW);
        }
    }

    public final void put() {
        if (BODY == null) {
            request(HttpMethod.PUT);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.PUT_RAW);
        }
    }

    public final void delete() {
        request(HttpMethod.DELETE);
    }

    public final void upload() {
        request(HttpMethod.UPLOAD);
    }

    public final void download() {
        new DownloadHandler(URL,PARAMS,DOWNLOAD_DIR, EXTENSION, NAME,
                iRequestCallback)
                .handleDownload();
    }

    private void request(HttpMethod method) {
        final RetrofitApiService service = RetrofitCreator.getApiService();
        Call<String> call = null;
        if (iRequestCallback != null) {
            iRequestCallback.onStart();
        }
        switch (method) {
            case GET:
                call = service.get(URL, PARAMS);
                break;
            case POST:
                call = service.post(URL, PARAMS);
                break;
            case POST_RAW:
                call = service.postRaw(URL, BODY);
                break;
            case PUT:
                call = service.put(URL, PARAMS);
                break;
            case PUT_RAW:
                call = service.putRaw(URL, BODY);
                break;
            case DELETE:
                call = service.delete(URL, PARAMS);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), FILE);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                call = service.upload(URL, body);
                break;
            default:
                break;
        }

        if (call != null) {
            call.enqueue(new RetrofitCallback(iRequestCallback));
        }
    }


}
