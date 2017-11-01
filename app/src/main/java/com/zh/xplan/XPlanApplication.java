package com.zh.xplan;

import android.app.Application;
import android.os.Process;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.module.common.image.ImageManager;
import com.module.common.log.LogUtil;
import com.module.common.utils.Utils;
import com.zh.xplan.ui.skin.Settings;
import com.zh.xplan.ui.skin.SkinChangeHelper;
import cn.bingoogolapple.swipebacklayout.BGASwipeBackManager;

/**
 * Created by zhenghao on 2017/5/5.
 */

public class XPlanApplication extends Application {
    private static XPlanApplication instance = null;
    public  synchronized static XPlanApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app init code...
        instance = this;
        initApplication();
    }

    public void initApplication() {
        Utils.init(this,AppConstant.isDebug).setBaseUrl(AppConstant.HTTP_HOST);
        LogUtil.init();
        initSkinLoader();
        // 必须在 Application 的 onCreate 方法中执行 BGASwipeBackManager.getInstance().init(this) 来初始化滑动返回
        BGASwipeBackManager.getInstance().init(this);
        ImageManager.init(this);
    }

    public void onDestroy() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        Settings.createInstance(this);
        // 初始化皮肤框架
        SkinChangeHelper.getInstance().init(this);
        //初始化上次缓存的皮肤
        SkinChangeHelper.getInstance().refreshSkin(null);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Fresco.getImagePipeline().clearMemoryCaches();
    }

}
