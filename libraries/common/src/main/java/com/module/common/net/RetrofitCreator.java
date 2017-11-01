package com.module.common.net;

import com.module.common.log.LogUtil;
import com.module.common.utils.Utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Retrofit 创建
 */
public final class RetrofitCreator {
    /**
     * 构建OkHttp
     */
    private static final class OKHttpHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();
        //设置缓存路径
        private static File httpCacheDirectory = new File(Utils.getContext().getCacheDir(), "okhttpCache");

        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .cache(new Cache(httpCacheDirectory, 10 * 1024 * 1024)) //设置缓存 10M
                .build();

        private static OkHttpClient.Builder addInterceptor() {
            //可以判断是否是debug模式，不是则不加拦截
            if(Utils.isDebug){
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                BUILDER.addInterceptor(loggingInterceptor);
            }
            return BUILDER;
        }
    }

    /**
     * 构建全局Retrofit客户端
     */
    private static final class RetrofitHolder {
        private static final String BASE_URL = Utils.HTTP_HOST;
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OKHttpHolder.OK_HTTP_CLIENT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    /**
     * Service接口
     */
    private static final class RetrofitServiceHolder {
        private static final RetrofitApiService API_SERVICE =
                RetrofitHolder.RETROFIT_CLIENT.create(RetrofitApiService.class);
    }

    /**
     * 获取Retrofit 网络请求api
     * @return
     */
    public static RetrofitApiService getApiService() {
        return RetrofitServiceHolder.API_SERVICE;
    }

    /**
     * 获取OkHttpClient 应用内最好是单例模式，不要再创建其他Client
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        return OKHttpHolder.OK_HTTP_CLIENT;
    }

    private static class HttpLogger implements HttpLoggingInterceptor.Logger {
        private StringBuilder mMessage = new StringBuilder();

        @Override
        public void log(String message) {
            // 请求或者响应开始
            if (message.startsWith("--> POST")) {
                mMessage.setLength(0);
            }
            // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
            if ((message.startsWith("{") && message.endsWith("}"))
                    || (message.startsWith("[") && message.endsWith("]"))) {
                message = JsonUtil.formatJson(message);
            }
            mMessage.append(message.concat("\n"));
            // 请求或者响应结束，打印整条日志
            if (message.startsWith("<-- END HTTP")) {
                LogUtil.d("okHttp",mMessage.toString());
                mMessage.setLength(0);
            }
        }
    }
}
