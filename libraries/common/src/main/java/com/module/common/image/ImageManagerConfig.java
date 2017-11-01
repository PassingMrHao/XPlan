package com.module.common.image;

import android.app.ActivityManager;
import android.content.Context;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.module.common.net.RetrofitCreator;
import com.module.common.image.okhttp3.OkHttpImagePipelineConfigFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ImageManagerConfig {

    protected static final String IMAGE_PIPELINE_CACHE_DIR = "image_cache";
    protected static final String IMAGE_PIPELINE_SMALL_CACHE_DIR = "image_small_cache";
    // 最大缓存大小、在低内存设备下的缓存大小、在极低内存设备下的缓存大小。默认值为40M、10M、2M。
    protected static final int MAX_DISK_CACHE_SIZE = 60 * ByteConstants.MB;
    protected static final int MAX_DISK_ON_LOWDISKSPACE_CACHE_SIZE = 30 * ByteConstants.MB;
    protected static final int MAX_DISK_ON_VERY_LOWDISKSPACE_CACHE_SIZE = 20 * ByteConstants.MB;

    protected static final int MAX_DISK_CACHE_SIZE_SMALL = 20 * ByteConstants.MB;
    protected static final int MAX_DISK_ON_LOWDISKSPACE_CACHE_SIZE_SMALL = 15 * ByteConstants.MB;
    protected static final int MAX_DISK_ON_VERY_LOWDISKSPACE_CACHE_SIZE_SMALL = 10 * ByteConstants.MB;

    protected Context mContext;
    protected ImagePipelineConfig mImagePipelineConfig;
    private static ImageManagerConfig sImageManagerConfig;

    protected ImageManagerConfig(Context context) {
        mContext = context.getApplicationContext();
        toggleLog();
    }

    public static ImageManagerConfig getInstance(Context context) {
        if(sImageManagerConfig == null) {
            synchronized (ImageManagerConfig.class) {
                if(sImageManagerConfig == null) {
                    sImageManagerConfig = new ImageManagerConfig(context);
                }
            }
        }
        return sImageManagerConfig;
    }

    /**
     * Creates config using android http stack as network backend.
     */
    public ImagePipelineConfig getImagePipelineConfig() {
        if (mImagePipelineConfig == null) {
            mImagePipelineConfig = createConfigBuilder()
//                    .setBitmapsConfig(Bitmap.Config.ARGB_8888) // 若不是要求忒高清显示应用，就用使用RGB_565吧（默认是ARGB_8888)
                    .setDownsampleEnabled(true) // 在解码时改变图片的大小，支持PNG、JPG以及WEBP格式的图片，与ResizeOptions配合使用
                    // 设置Jpeg格式的图片支持渐进式显示
//                    .setProgressiveJpegConfig(new ProgressiveJpegConfig() {
//                        @Override
//                        public int getNextScanNumberToDecode(int scanNumber) {
//                            return scanNumber + 2;
//                        }
//
//                        public QualityInfo getQualityInfo(int scanNumber) {
//                            boolean isGoodEnough = (scanNumber >= 5);
//                            return ImmutableQualityInfo.of(scanNumber, isGoodEnough, false);
//                        }
//                    })
//                    .setRequestListeners(getRequestListeners())
                    .setMemoryTrimmableRegistry(getMemoryTrimmableRegistry()) // 报内存警告时的监听
                    // 设置内存配置
                    .setBitmapMemoryCacheParamsSupplier(new BitmapMemoryCacheParamsSupplier(
                            (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)))
                    .setMainDiskCacheConfig(getMainDiskCacheConfig()) // 设置主磁盘配置
                    .setSmallImageDiskCacheConfig(getSmallDiskCacheConfig()) // 设置小图的磁盘配置
                    .build();
        }
        return mImagePipelineConfig;
    }

//    /**
//     * Create ImagePipelineConfig Builder   使用默认的httpurlconnection 网络请求
//     * @return
//     */
//    protected ImagePipelineConfig.Builder createConfigBuilder() {
//        return ImagePipelineConfig.newBuilder(mContext);
//    }

    /**
     * 使用OKHttp3替换原有的网络请求
     * @return
     */
    protected ImagePipelineConfig.Builder createConfigBuilder() {
        // LOG过滤标签 OkHttpClient最好使用单例
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .retryOnConnectionFailure(false)
//                .build();
        return OkHttpImagePipelineConfigFactory.newBuilder(mContext, RetrofitCreator.getOkHttpClient());
    }

    /**
     * 当内存紧张时采取的措施
     * @return
     */
    protected MemoryTrimmableRegistry getMemoryTrimmableRegistry() {
        MemoryTrimmableRegistry memoryTrimmableRegistry = NoOpMemoryTrimmableRegistry.getInstance();
        memoryTrimmableRegistry.registerMemoryTrimmable(new MemoryTrimmable() {
            @Override
            public void trim(MemoryTrimType trimType) {
                final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();
                if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio
                        ) {
                    // 清除内存缓存
                    Fresco.getImagePipeline().clearMemoryCaches();
                }
            }
        });
        return memoryTrimmableRegistry;
    }

    /**
     * LOG开关
     */
    protected void toggleLog() {
        // 你可以通过下面这条shell命令来查看Fresco日志：
        // adb logcat -v threadtime | grep -iE 'LoggingListener|AbstractDraweeController|BufferedDiskCache'
//        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
    }

    /**
     * 设置网络请求监听
     * @return
     */
    protected Set<RequestListener> getRequestListeners() {
        Set<RequestListener> requestListeners = new HashSet<>();
//        requestListeners.add(new RequestLoggingListener());
        return requestListeners;
    }

    /**
     * 获取主磁盘配置
     * @return
     */
    protected DiskCacheConfig getMainDiskCacheConfig() {
        /**
         * 推荐缓存到应用本身的缓存文件夹，这么做的好处是:
         * 1、当应用被用户卸载后能自动清除缓存，增加用户好感（可能以后用得着时，还会想起我）
         * 2、一些内存清理软件可以扫描出来，进行内存的清理
         */
        File fileCacheDir = new File(mContext.getFilesDir() + "/Fresco/main");
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                fileCacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fresco");
//            }

        return DiskCacheConfig.newBuilder(mContext)
                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
                .setBaseDirectoryPath(fileCacheDir)
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_ON_LOWDISKSPACE_CACHE_SIZE)
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_ON_VERY_LOWDISKSPACE_CACHE_SIZE)
                .build();
    }

    /**
     * 获取小图的磁盘配置（辅助）
     * @return
     */
    protected DiskCacheConfig getSmallDiskCacheConfig() {
        /**
         * 推荐缓存到应用本身的缓存文件夹，这么做的好处是:
         * 1、当应用被用户卸载后能自动清除缓存，增加用户好感（可能以后用得着时，还会想起我）
         * 2、一些内存清理软件可以扫描出来，进行内存的清理
         */
        File fileCacheDir = new File(mContext.getFilesDir() + "/Fresco/small");
        //            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                fileCacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fresco");
//            }
        return DiskCacheConfig.newBuilder(mContext)
                .setBaseDirectoryPath(fileCacheDir)
                .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE_SMALL)
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_ON_LOWDISKSPACE_CACHE_SIZE_SMALL)
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_ON_VERY_LOWDISKSPACE_CACHE_SIZE_SMALL)
                .build();
    }

}
