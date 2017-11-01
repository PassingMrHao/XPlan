package com.module.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.module.common.log.LogUtil;

import java.io.File;

/**
 * Created by zhenghao on 2017/8/28.
 * 图片加载管理
 */
public class ImageManager {
    private static final String TAG = ImageManager.class.getSimpleName();
    private ImageManager() {
    }

    public static void init(Context context) {
        Fresco.initialize(context, ImageManagerConfig.getInstance(context).getImagePipelineConfig());
    }

    public static void displayImage(SimpleDraweeView draweeView, String url) {
        if (draweeView == null || TextUtils.isEmpty(url)) {
            return;
        }
        draweeView.setImageURI(url);
    }

    public static void displayImage(SimpleDraweeView draweeView, File file) {
        if (file == null || draweeView == null) {
            return;
        }
        Uri uri = Uri.fromFile(file);
        if (uri == null){
            return;
        }
        draweeView.setImageURI(uri);
    }

    public static void displayImage(SimpleDraweeView draweeView, Uri uri) {
        if (uri == null || draweeView == null) {
            return;
        }
        draweeView.setImageURI(uri);
    }

    public static void displayImage(SimpleDraweeView draweeView, Uri uri, int width, int height) {
        if (uri == null || draweeView == null) {
            return;
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(draweeView.getController())
                .setImageRequest(request)
                .build();
        draweeView.setController(controller);
    }

    public static void displayImage(SimpleDraweeView draweeView, String url, int width, int height) {
        if (TextUtils.isEmpty(url) || draweeView == null ) {
            return;
        }
        Uri uri = Uri.parse(url);
        displayImage(draweeView, uri, width, height);
    }

    public static void prefetchPhoto(Context context, Uri uri, int width, int height) {
        if (uri == null) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .setAutoRotateEnabled(true)
                .setRequestPriority(Priority.LOW)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();

        Fresco.getImagePipeline().prefetchToDiskCache(request, context);
    }

    public static void prefetchPhoto(Context context, String url, int width, int height) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        Uri uri = Uri.parse(url);
        prefetchPhoto(context, uri, width, height);
    }

    //获取Fresco磁盘缓存中的图片
    public static File obtainCachedPhotoFile(CacheKey cacheKey) {
        File localFile = null;
        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource binaryResource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);

                localFile = ((FileBinaryResource) binaryResource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource binaryResource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) binaryResource).getFile();
            }
        }

        return localFile;
    }

    public static void pause(){
        Fresco.getImagePipeline().pause();
    }

    public static void resume(){
        Fresco.getImagePipeline().resume();
    }

    /**
     * 监听加载进度
     * @param uri
     * @param draweeView
     * @param width
     * @param height
     * @param controllerListener
     */
    public static void displayImage(Uri uri, SimpleDraweeView draweeView, int width, int height, BaseControllerListener controllerListener ){
        ImageRequest request = null;
        if(width != 0){
            request = ImageRequestBuilder
                    .newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
        }
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setImageRequest(request)
                .setOldController(draweeView.getController())//列表滑动会变顺畅
                .setControllerListener(controllerListener)
                .build();
        draweeView.setController(controller);
    }

    /**
     * 从内存缓存中移除指定图片的缓存
     *
     * @param uri
     */
    public static void evictFromMemoryCache(final Uri uri) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (imagePipeline.isInBitmapMemoryCache(uri)) {
            imagePipeline.evictFromMemoryCache(uri);
        }
    }

    /**
     * 从磁盘缓存中移除指定图片的缓存
     *
     * @param uri
     */
    public static void evictFromDiskCache(final Uri uri) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (imagePipeline.isInDiskCacheSync(uri)) {
            imagePipeline.evictFromDiskCache(uri);
        }
    }

    /**
     * 移除指定图片的所有缓存（包括内存+磁盘）
     *
     * @param uri
     */
    public static void evictFromCache(final Uri uri) {
        evictFromMemoryCache(uri);
        evictFromDiskCache(uri);
    }

    /**
     * 清空所有内存缓存
     */
    public static void clearMemoryCaches() {
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    /**
     * 清空所有磁盘缓存，若你配置有两个磁盘缓存，则两个都会清除
     */
    public static void clearDiskCaches() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    /**
     * 清除所有缓存（包括内存+磁盘）
     */
    public static void clearAllCaches() {
        clearMemoryCaches();
        clearDiskCaches();
    }

    /**
     * 获取所有Fresco缓存大小
     * @return
     */
    public static long getAllCacheSize() {
        return getMainDiskStorageCacheSize() + getSmallDiskStorageCacheSize();
    }

    /**
     * 获取磁盘上主缓存文件缓存的大小
     *
     * @return
     */
    public static long getMainDiskStorageCacheSize() {
        Fresco.getImagePipelineFactory().getMainFileCache().trimToMinimum();
        return Fresco.getImagePipelineFactory().getMainFileCache().getSize();
    }

    /**
     * 获取磁盘上副缓存（小文件）文件缓存的大小
     *
     * @return
     */
    public static long getSmallDiskStorageCacheSize() {
        Fresco.getImagePipelineFactory().getSmallImageFileCache().trimToMinimum();
        return Fresco.getImagePipelineFactory().getSmallImageFileCache().getSize();
    }


    public interface IResult<T> {

        void onSuccess(T result);

        void onFail();
    }


    /**
     * 从本地文件或网络获取Bitmap
     * @param context
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @param loadImageResult
     */
    public static void displayImage(Context context, String url, final int reqWidth, final int reqHeight, final IResult<Bitmap> loadImageResult) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e("从本地文件或网络获取Bitmap url is null");
            loadImageResult.onFail();
            return;
        }

        Uri uri = Uri.parse(url);
        if(!UriUtil.isNetworkUri(uri)) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_FILE_SCHEME)
                    .path(url)
                    .build();
        }

        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (reqWidth > 0 && reqHeight > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(reqWidth, reqHeight));
        }
        ImageRequest imageRequest = imageRequestBuilder.build();

        // 获取已解码的图片，返回的是Bitmap
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, context);
        DataSubscriber dataSubscriber = new BaseDataSubscriber<CloseableReference<CloseableBitmap>>() {
            @Override
            public void onNewResultImpl(DataSource<CloseableReference<CloseableBitmap>> dataSource) {
                if (!dataSource.isFinished()) {
                    loadImageResult.onFail();
                    return;
                }

                CloseableReference<CloseableBitmap> imageReference = dataSource.getResult();
                boolean flag = false;
                if (imageReference != null) {
                    final CloseableReference<CloseableBitmap> closeableReference = imageReference.clone();
                    try {
                        CloseableBitmap closeableBitmap = closeableReference.get();
                        Bitmap bitmap = closeableBitmap.getUnderlyingBitmap();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            final Bitmap tempBitmap = bitmap.copy(bitmap.getConfig(), false);
                            flag = true;
                            loadImageResult.onSuccess(tempBitmap);
                        }
                    } finally {
                        imageReference.close();
                        closeableReference.close();
                    }
                }
                if(!flag){
                    loadImageResult.onFail();
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                loadImageResult.onFail();
                Throwable throwable = dataSource.getFailureCause();
                if (throwable != null) {
                    Log.e("ImageManager", "onFailureImpl = " + throwable.toString());
                }
            }
        };
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

}
