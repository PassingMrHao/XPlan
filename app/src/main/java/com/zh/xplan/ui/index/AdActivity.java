package com.zh.xplan.ui.index;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.jaeger.library.StatusBarUtil;
import com.module.common.log.LogUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.index.util.FileUtils;
import com.zh.xplan.ui.mainactivity.MainActivity;
import java.io.File;

/**
 * 广告界面
 */
public class AdActivity extends Activity implements View.OnClickListener {
    private static final String SPLASH_FILE_NAME = "splash";
    private ImageView ivSplash;
    private TextView tvCopyright;
    private Button mBtnJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StatusBarUtil.setTranslucentForImageView(this,0,null);//状态栏透明
        ivSplash = (ImageView) findViewById(R.id.iv_splash);
        mBtnJump = (Button) findViewById(R.id.btn_jump);
        mBtnJump.setOnClickListener(this);
        tvCopyright = (TextView) findViewById(R.id.tv_copyright);
        tvCopyright.setText("Copyright © 2016--2017 XPlan");
        showSplash();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.sp_bg:
//                gotoWebActivity();
//                break;
            case R.id.btn_jump:
                gotoMainActivity();
                break;
            default:
                break;
        }
    }

    private void showSplash() {
        File splashImg = new File(FileUtils.getSplashDir(getApplicationContext()), SPLASH_FILE_NAME);
        if (splashImg.exists()) {
            LogUtil.e("zh","splashImg.exists() ");
            Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getPath());
            if(bitmap != null){
                LogUtil.e("zh","FileUtils.bitmap ");
                ivSplash.setImageBitmap(bitmap);
                countDownTimer.start();
                mBtnJump.setVisibility(View.VISIBLE);
                return;
            }
        }else{
            LogUtil.e("zh","splashImg not exists() ");
        }
        gotoMainActivity();
    }

    private CountDownTimer countDownTimer = new CountDownTimer(1200, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mBtnJump.setText("跳过(" + millisUntilFinished / 1000 + "s)");
        }

        @Override
        public void onFinish() {
            mBtnJump.setText("跳过(" + 0 + "s)");
            gotoMainActivity();
        }
    };

    private void gotoMainActivity(){
        // 跳转到主界面
        startActivity(new Intent(AdActivity.this,
                MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
