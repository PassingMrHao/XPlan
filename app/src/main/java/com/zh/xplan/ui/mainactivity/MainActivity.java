package com.zh.xplan.ui.mainactivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.module.common.log.LogUtil;
import com.module.common.net.HttpManager;
import com.module.common.net.callback.IRequestCallback;
import com.zh.xplan.AppConstant;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.base.FragmentsFactory;
import com.zh.xplan.ui.index.util.FileUtils;
import com.zh.xplan.ui.index.model.AdModel;
import com.zh.xplan.ui.iptools.IpToolsActivity;
import com.zh.xplan.ui.pulltorefresh.PullToRefreshDemoActivity;
import com.zh.xplan.ui.skin.SkinChangeHelper;
import com.zh.xplan.ui.skin.SkinConfigHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity
        implements View.OnClickListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private long ExitTime; // 延时退出应用时间变量
    private RadioGroup mRadioGroup; // 底部菜单栏
    private List<RadioButton> radioBtnList = new ArrayList<RadioButton>();
    private final int BOTTON_MENU1 = 0;
    private final int BOTTON_MENU2 = 1;
    private RadioButton rb_menu_pic,rb_menu_setting;
    private Drawable drawable;
    private static FragmentManager mFragmentManager;
    private static BaseFragment mCurrentFragment;// 当前FrameLayout中显示的Fragment
    private static Boolean isFirst = true;// 是否是第一次进入应用
    private SwitchCompat night_mode_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        // 设置默认选中第一个菜单
        mFragmentManager = getSupportFragmentManager();
        mRadioGroup.check(R.id.rb_menu_pic);
        initDatas();
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    public boolean isSupportSkinChange() {
        return true;
    }

    @Override
    public boolean isSwitchSkinImmediately() {
        return true;
    }

    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, getResources().getColor(R.color.colorPrimaryDark));
//        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        View headerView = navigationView.getHeaderView(0);
        View headerView = drawerLayout;
        headerView.findViewById(R.id.ll_refresh).setOnClickListener(this);
        headerView.findViewById(R.id.ll_ip_tools).setOnClickListener(this);
        headerView.findViewById(R.id.ll_night_mode).setOnClickListener(this);
        night_mode_switch = (SwitchCompat) headerView.findViewById(R.id.night_mode_switch);
        changeNightModeSwitch();
        night_mode_switch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(SkinConfigHelper.isDefaultSkin() == isChecked ){
                    LogUtil.e("zh","onCheckedChanged  " + isChecked);
                    SkinChangeHelper.getInstance().switchSkinMode(
                            new SkinChangeHelper.OnSkinChangeListener() {
                                @Override
                                public void onSuccess() {
                                    LogUtil.e("zh","换肤成功");
                                    changeStatusBarColor();
                                }

                                @Override
                                public void onError() {
                                    LogUtil.e("zh","换肤失败");
                                }
                            }
                    );
                }
            }
        });
        initBottomMenus();
    }

    /**
     * 初始化底部菜单的图片。（也可以扩展动态更新菜单文字）
     */
    private void initBottomMenus() {
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_menu_group);
        rb_menu_pic = (RadioButton) findViewById(R.id.rb_menu_pic);
        rb_menu_setting = (RadioButton) findViewById(R.id.rb_menu_setting);
        radioBtnList.add(rb_menu_pic);
        radioBtnList.add(rb_menu_setting);
        for (int i = 0; i < radioBtnList.size(); i++) {
            if (i == BOTTON_MENU1) {
                drawable = this.getResources().getDrawable(
                        R.drawable.menu_bottom_pic_bg);
            } else if (i == BOTTON_MENU2) {
                drawable = this.getResources().getDrawable(
                        R.drawable.menu_bottom_my_bg);
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            radioBtnList.get(i)
                    .setCompoundDrawables(null, drawable, null, null);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int position) {
                switch (position) {
                    case R.id.rb_menu_pic:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(0));
                        break;
                    case R.id.rb_menu_setting:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(1));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_refresh:// 下拉刷新
                startActivity(new Intent(this,PullToRefreshDemoActivity.class));
                break;
            case R.id.ll_ip_tools:// ip工具
                startActivity(new Intent(this,IpToolsActivity.class));
                break;
            case R.id.ll_night_mode:// 夜间模式
                SkinChangeHelper.getInstance().switchSkinMode(
                    new SkinChangeHelper.OnSkinChangeListener() {
                        @Override
                        public void onSuccess() {
                            LogUtil.e(TAG,"换肤成功");
                            changeStatusBarColor();
                            changeNightModeSwitch();
                        }
                        @Override
                        public void onError() {
                        }
                    }
                );
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void openDrawer() {
        if(drawerLayout != null){
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void initDatas() {
        updateAdPicture();
    }

    private void changeStatusBarColor(){
        int color = SkinConfigHelper.isDefaultSkin() ? getResources().getColor(R.color.colorPrimaryDark) : getResources().getColor(R.color.colorPrimaryDark_night);
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, color);
    }

    private void changeNightModeSwitch(){
        if(SkinConfigHelper.isDefaultSkin()){
            night_mode_switch.setChecked(false);
        }else{
            night_mode_switch.setChecked(true);
        }
    }

    /**
     * 切换显示不同的fragment
     */
    public static  void setFragment(Fragment fromFragment, Fragment toFragment) {
        if (isFirst == true) {
            // 如果是第一次进入应用，把菜单1对应的fragment加载进去，并显示
            FragmentTransaction transaction = mFragmentManager
                    .beginTransaction();
            transaction.add(R.id.id_content, toFragment).commit();
            mCurrentFragment = (BaseFragment) toFragment;
            isFirst = false;
            return;
        }
        if (mCurrentFragment != toFragment) {
            // 隐藏之前的fragment,显示下一个fragment
            mCurrentFragment = (BaseFragment) toFragment;
            FragmentTransaction transaction = mFragmentManager
                    .beginTransaction();
            if (!toFragment.isAdded()) {
                transaction.hide(fromFragment).add(R.id.id_content, toFragment).commit();
            } else {
                transaction.hide(fromFragment).show(toFragment).commit();
            }
        }
    }

    /**
     * 按两下返回键退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果侧滑菜单是打开的，则关闭
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                // Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                // 自定义Toast的样式
                Toast toast = new Toast(this);
                View view = LayoutInflater.from(this).inflate(
                        R.layout.toast_exit_app, null);
                TextView textView = (TextView) view
                        .findViewById(R.id.tv_exit_toast);
                textView.setText("再按一次退出程序");
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
                ExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        XPlanApplication.getInstance().onDestroy();
        super.onDestroy();
    }

    /**
     * 更新启动页广告图片
     */
    private void updateAdPicture() {
        HttpManager.builder()
                .url(AppConstant.SPLASH_URL)
                .requestCallbak(new IRequestCallback() {

                    @Override
                    public void onSuccess(String response) {
                        AdModel adModel = new Gson().fromJson(response,AdModel.class);
                        if (adModel == null || TextUtils.isEmpty(adModel.getUrl())) {
                            LogUtil.e("zh","response == null" );
                            return;
                        }
                        final String url = adModel.getUrl();
                        LogUtil.e("zh","response != null url " + url);
                        String lastImgUrl = FileUtils.getSplashUrl();
                        if (url == null || TextUtils.isEmpty(url) || TextUtils.equals(lastImgUrl, url)) {
                            return;
                        }
                        //下载新的图片
                        HttpManager.builder()
                                .url(url)
                                .dir(FileUtils.getSplashDir(getApplicationContext()))
                                .name("splash")
                                .requestCallbak(new IRequestCallback() {
                                    @Override
                                    public void onSuccess(String response) {
                                        LogUtil.e("zh","download onSuccess , url) " + response);
                                        FileUtils.saveSplashUrl(url);
                                    }

                                    @Override
                                    public void onError(int code, String msg) {
                                        LogUtil.e("zh","download onError , url) " + msg);
                                    }
                                })
                                .build()
                                .download();
                    }

                    @Override
                    public void onError(int code, String msg) {
                    }
                })
                .build()
                .get();
    }
}
