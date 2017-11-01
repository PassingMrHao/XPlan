package com.zh.xplan.ui.index;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.module.common.log.LogUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.index.util.FileUtils;
import com.zh.xplan.ui.mainactivity.MainActivity;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 启动界面
 */
public class IndexActivity extends Activity {
	private static final String SPLASH_FILE_NAME = "splash";

	private final IndexActivity.MyHandler mHandler = new IndexActivity.MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<IndexActivity> mActivity;

		public MyHandler(IndexActivity activity) {
			mActivity = new WeakReference<IndexActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final IndexActivity activity = mActivity.get();
			if (activity != null) {
				switch (msg.what) {
					case 0:
						// 跳转到主界面
						File splashImg = new File(FileUtils.getSplashDir(activity.getApplicationContext()), SPLASH_FILE_NAME);
						if (splashImg.exists()) {
							LogUtil.e("zh","splashImg.exists() ");
							Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getPath());
							if(bitmap != null){
								LogUtil.e("zh","FileUtils.bitmap ");
								activity.startActivity(new Intent(activity,
										AdActivity.class));
								activity.finish();
								return;
							}
						}else{
							LogUtil.e("zh","IndexActivity splashImg not exists() ");
						}
						activity.startActivity(new Intent(activity,
								MainActivity.class));
						activity.finish();
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//第一次安装应用在系统的安装器安装完成界面有“完成”和“打开”两个按钮。
		// 当用户点击“打开”按钮并进行了一些操作后，若此时用户点击Home键切出应用到桌面，
//        再从桌面点击应用程序图标试图切回应用接着刚才的操作继续操作时，应用重新到了初始界面，
//        此时之前从系统的安装完成界面点击打开启动的应用其实还在后面运行。
//        一是、如果上面的Activity中实现了finish() 和 onDestroy() 方法，一定要保证这两个方法中
// 不会有对空对象的操作以及注销未注册的广播等类似操作，因为第二次打开应用时，程序会调用finish()方法，
// 及触发onDestroy()方法，而这两个函数里面的对象变量都还未进行初始化等操作。二是、finish() 和 onDestroy()
// 方法中不能有System.exit(0);否则第二次打开应用杀掉进程时也会将第一次打开的应用杀掉。
		if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
			finish();
			return;
		}
		//==========================
		setContentView(R.layout.activity_index);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.sendEmptyMessageDelayed(0,400);
	}

	@Override
	public void onBackPressed() {
	}
}
