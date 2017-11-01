package com.zh.xplan.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.jaeger.library.StatusBarUtil;
import com.zh.xplan.R;
import com.module.common.log.LogUtil;
import com.zh.xplan.ui.skin.SkinConfigHelper;

import org.qcode.qskinloader.IActivitySkinEventHandler;
import org.qcode.qskinloader.ISkinActivity;
import org.qcode.qskinloader.SkinManager;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper;

/**
 * Activity 基类 便于统一管理
 */
public class BaseActivity extends AppCompatActivity  implements BGASwipeBackHelper.Delegate,ISkinActivity {
    public final String TAG = getClass().getSimpleName();
    protected Activity mActivity;
    protected BGASwipeBackHelper mSwipeBackHelper;//滑动返回
    public IActivitySkinEventHandler mSkinEventHandler;//皮肤切换
    private boolean mFirstTimeApplySkin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        initSkinEventHandler();
        initSwipeBackFinish();
        super.onCreate(savedInstanceState);
    }

    /**
     * 初始化换肤
     */
    private void initSkinEventHandler() {
        mSkinEventHandler = SkinManager.newActivitySkinEventHandler()
                .setSwitchSkinImmediately(isSwitchSkinImmediately())
                .setSupportSkinChange(isSupportSkinChange())
                .setWindowBackgroundResource(getWindowBackgroundResource())
                .setNeedDelegateViewCreate(true);
        mSkinEventHandler.onCreate(this);
    }

    /**
     * 初始化滑动返回。在 super.onCreate(savedInstanceState) 之前调用该方法
     */
    private void initSwipeBackFinish() {
        mSwipeBackHelper = new BGASwipeBackHelper(this, this);
        // 「必须在 Application 的 onCreate 方法中执行 BGASwipeBackManager.getInstance().init(this) 来初始化滑动返回」
        // 下面几项可以不配置，这里只是为了讲述接口用法。
        // 设置滑动返回是否可用。默认值为 true
        mSwipeBackHelper.setSwipeBackEnable(true);
        // 设置是否仅仅跟踪左侧边缘的滑动返回。默认值为 true
        mSwipeBackHelper.setIsOnlyTrackingLeftEdge(true);
        // 设置是否是微信滑动返回样式。默认值为 true
        mSwipeBackHelper.setIsWeChatStyle(true);
        // 设置阴影资源 id。默认值为 R.drawable.bga_sbl_shadow
        mSwipeBackHelper.setShadowResId(R.drawable.bga_sbl_shadow);
        // 设置是否显示滑动返回的阴影效果。默认值为 true
        mSwipeBackHelper.setIsNeedShowShadow(true);
        // 设置阴影区域的透明度是否根据滑动的距离渐变。默认值为 true
        mSwipeBackHelper.setIsShadowAlphaGradient(true);
        // 设置触发释放后自动滑动返回的阈值，默认值为 0.3f
        mSwipeBackHelper.setSwipeBackThreshold(0.3f);
    }

    /**
     * 触摸空白区域自动隐藏键盘
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }


    /**
     * 是否支持滑动返回。这里在父类中默认返回 true 来支持滑动返回，如果某个界面不想支持滑动返回则重写该方法返回 false 即可
     * @return
     */
    @Override
    public boolean isSupportSwipeBack() {
        return true;
    }

    /**
     * 正在滑动返回
     * @param slideOffset 从 0 到 1
     */
    @Override
    public void onSwipeBackLayoutSlide(float slideOffset) {
    }

    /**
     * 没达到滑动返回的阈值，取消滑动返回动作，回到默认状态
     */
    @Override
    public void onSwipeBackLayoutCancel() {
    }

    /**
     * 滑动返回执行完毕，销毁当前 Activity
     */
    @Override
    public void onSwipeBackLayoutExecuted() {
        mSwipeBackHelper.swipeBackward();
    }

    @Override
    public void onBackPressed() {
        // 正在滑动返回的时候取消返回按钮事件
        if (mSwipeBackHelper.isSliding()) {
            return;
        }
        mSwipeBackHelper.backward();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mSkinEventHandler.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //皮肤相关，此通知放在此处，尽量让子类的view都添加到view树内
        if (mFirstTimeApplySkin) {
            mSkinEventHandler.onViewCreated();
            mFirstTimeApplySkin = false;
        }
        mSkinEventHandler.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mSkinEventHandler.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSkinEventHandler.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSkinEventHandler.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSkinEventHandler.onDestroy();
    }


    @Override
    public boolean isSupportSkinChange() {
        //告知当前界面是否支持换肤：true支持换肤，false不支持
        return false;
    }
//    /**
//     * true：默认支持所有View换肤，不用添加skin:enable="true"，不想支持则设置skin:enable="false"
//     * false：默认不支持所有View换肤，对需要换肤的View添加skin:enable="true"
//     *
//     * @return
//     */
//    @Override
//    public boolean isSupportAllViewSkin() {
//        return false;
//    }

    @Override
    public boolean isSwitchSkinImmediately() {
        //告知当切换皮肤时，是否立刻刷新当前界面；true立刻刷新，false表示在界面onResume时刷新；
        //减轻换肤时性能压力
        return false;
    }

    @Override
    public void handleSkinChange() {
        //当前界面在换肤时收到的回调，可以在此回调内做一些其他事情；
        //比如：通知WebView内的页面切换到夜间模式等
        LogUtil.e("zh","换肤成功11" + SkinConfigHelper.isDefaultSkin());
    }

    /**
     * 告知当前界面Window的background资源，换肤时会寻找对应的资源替换
     */
    protected int getWindowBackgroundResource()
    {
        return 0;
    }


    /**
     * 设置状态栏颜色
     * @param color
     */
    protected void setStatusBarColor(@ColorInt int color) {
        setStatusBarColor(color, StatusBarUtil.DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 设置状态栏颜色
     * @param color
     * @param statusBarAlpha 透明度
     */
    public void setStatusBarColor(@ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
        StatusBarUtil.setColorForSwipeBack(this, color, statusBarAlpha);
    }
}
