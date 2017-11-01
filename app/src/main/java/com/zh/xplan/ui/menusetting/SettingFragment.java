package com.zh.xplan.ui.menusetting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.module.common.image.ImageManager;
import com.module.common.view.roundimageview.RoundImageView;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.iptools.IpToolsActivity;
import com.zh.xplan.ui.robot.RobotActivity;
import com.zh.xplan.ui.utils.TitleUtil;

import org.qcode.qskinloader.SkinManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;


/**
 * 设置菜单
 * Created by zh
 */
@RuntimePermissions
public class SettingFragment extends BaseFragment implements OnClickListener {
	private LinearLayout mClearHistory,mGoMarket,mRobot,mIpTools;
	private RelativeLayout mChackVersion;
	private TextView mCurrentVersion;
	private TextView mCache; //缓存大小
	private ProgressDialog mProgressDialog;//清理缓存时的对话框
	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果
	private RoundImageView mHeadPicture; //圆形头像
	private Bitmap bitmap;
	/* 头像名称 */
	private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	private static String HEAD_PATH = XPlanApplication.getInstance().getExternalFilesDir(null).getAbsolutePath() + "/head.jpg";
	private File tempFile;

	@SuppressLint("HandlerLeak")
	private Handler mHander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int value = msg.what;
			switch (value) {
			case 1:
				mCache.setText(msg.getData().getString("cacheSize"));
				break;
			case 2:
				mProgressDialog.dismiss();
				mCache.setText(msg.getData().getString("cacheSize"));
				break;
			default:
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@onCreateView菜单4");
		View mView = View.inflate(getActivity(),
				R.layout.fragment_setting, null);
		initTitle(getActivity(), mView);
		initView(mView);
		return mView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SkinManager.getInstance().applySkin(view, true);
	}

	/**
	 * 每次进入设置页面都更新下缓存的大小
	 */
	@Override
	public void onResume() {
		super.onResume();
		updateCacheSize();
	}
	/**
	 * 每次进入设置页面都更新下缓存的大小
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		if(hidden == false){
			updateCacheSize();
		}
		super.onHiddenChanged(hidden);
	}
	
	private void updateCacheSize(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = 1;
				Bundle bundle = new Bundle();
				bundle.putString("cacheSize", getDatabaseSize());
				msg.setData(bundle);
				mHander.sendMessage(msg);
			}
		}).start();
	}
	
	/**
	 * 初始化标题栏
	 * @param activity
	 * @param view
	 */
	private void initTitle(Activity activity, View view) {
		// 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		new TitleUtil(activity, view).setMiddleTitleText("设置");
	}
	
	/**
	 * 初始化界面
	 * @param mView
	 */
	private void initView(View mView) {
		mHeadPicture = (RoundImageView) mView.findViewById(R.id.iv_head_picture);
		TextView versionName = (TextView) mView
				.findViewById(R.id.tv_version_name);
		mChackVersion = (RelativeLayout) mView
				.findViewById(R.id.ll_chack_version);
		mCurrentVersion = (TextView) mView.findViewById(R.id.tv_current_version);
		mClearHistory = (LinearLayout) mView.findViewById(R.id.ll_clear);
		mCache = (TextView) mView.findViewById(R.id.tv_cache);
		mGoMarket = (LinearLayout) mView.findViewById(R.id.ll_go_market);
		mRobot = (LinearLayout) mView.findViewById(R.id.ll_robot);
		mIpTools = (LinearLayout) mView.findViewById(R.id.ll_ip_tools);
		mHeadPicture.setOnClickListener(this);
		mChackVersion.setOnClickListener(this);
		mClearHistory.setOnClickListener(this);
		mGoMarket.setOnClickListener(this);
		mRobot.setOnClickListener(this);
		mIpTools.setOnClickListener(this);
		Bitmap bt = getHead(HEAD_PATH);
		if (bt != null) {
			Drawable drawable = new BitmapDrawable(bt);
			mHeadPicture.setImageDrawable(drawable);
		} else {
			mHeadPicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.head_default));
		}
		try {
			PackageManager pm = getActivity().getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
			mCurrentVersion.setText("当前版本:" + pi.versionName);
			mCache.setText(getDatabaseSize());//缓存大小
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initDatas() {

	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.iv_head_picture:
				//编辑头像
				changeHeadPicture();
				break;
			case R.id.ll_clear:
				//清除缓存
				clearHistory();
				break;
			case R.id.ll_go_market:
				//市场评分
				goToMarket();
				break;
			case R.id.ll_robot:
				//小机器人
				startActivity(new Intent(getActivity(), RobotActivity.class));
				break;
			case R.id.ll_ip_tools:
				//IP工具
				startActivity(new Intent(getActivity(), IpToolsActivity.class));
				break;
			default:
				break;
		}
	}
	
	/**
	 * 去应用市场评分
	 */
	private void goToMarket() {
		if (!isMarketInstalled(getActivity())) {
			Toast.makeText(getActivity(), "您的手机没有安装应用市场", Toast.LENGTH_SHORT).show();
		    return;
		}
		try {
			//Uri uri = Uri.parse("market://details?id="+getPackageName());
			Uri uri = Uri.parse("market://details?id=" + "com.tencent.mobileqq");  
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			    startActivity(intent);
			}
		} catch (Exception e) {
			// 该功能部分手机可能有问题，待验证。详情参见http://blog.csdn.net/wangfayinn/article/details/10351655
			// 也可以调到某个网页应用市场
			Toast.makeText(getActivity(), "手机没有安装应用市场", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 本手机是否安装了应用市场
	 * @param context
	 * @return
	 */
	public static boolean isMarketInstalled(Context context) {
		Intent intent = new Intent();
		intent.setData(Uri.parse("market://details?id=android.browser"));
		List list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return 0 != list.size();
	}

	/**
	 * 替换头像对话框
	 */
	public void changeHeadPicture() {
		View view = View.inflate(getActivity(),R.layout.dialog_change_head_picture, null);
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		final AlertDialog dialog = new  AlertDialog.Builder(getActivity())
			.setTitle("更换头像")
			.setView(view)
			.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
		view.findViewById(R.id.ll_from_camera).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 从相机截取头像
				SettingFragmentPermissionsDispatcher.cameraWithCheck(SettingFragment.this);
				dialog.dismiss();
			}
		});
		view.findViewById(R.id.ll_from_gallery).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 从图库截取头像
				gallery();
				dialog.dismiss();
			}
		});
	}

	/**
	 * 清除历史记录
	 */
	public void clearHistory() {
		mProgressDialog = ProgressDialog.show(getActivity(), null, "清除记录中...",
				true, false);
		new Thread(new Runnable() {

			@Override
			public void run() {
				cleanDatabase();
				// 通知历史记录发生变化
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Message msg = Message.obtain();
				msg.what = 2;
				Bundle bundle = new Bundle();
				bundle.putString("cacheSize", getDatabaseSize());
				msg.setData(bundle);
				mHander.sendMessage(msg);
			}
		}).start();
		
	}

	/**
	 * 获取数据库文件的大小
	 * 
	 * @return
	 */
	private String getDatabaseSize() {
		try {
			long bDatabasesSize = CleanCacheUtils.getInternalFileSize(new File(
					"/data/data/" + getActivity().getPackageName()
							+ "/databases"));
			long wDatabasesSize = CleanCacheUtils.getExternalFileSize(new File(
					"/mnt/sdcard/android/data/"
							+ getActivity().getPackageName() + "/databases"));

			  /*Long diskLruCacheSize = 0L;
			  if(mDiskLruCache != null){
				  diskLruCacheSize = mDiskLruCache.size();
			  }*/
			return CleanCacheUtils.getFormatSize(bDatabasesSize
					+ wDatabasesSize + ImageManager.getAllCacheSize()) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 清除本应用数据库文件 和mDiskLruCache的大小
	 * @return
	 */
	private void cleanDatabase() {
		/*if(mDiskLruCache != null){
			try {
				mDiskLruCache.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		ImageManager.clearAllCaches();
		try {
			CleanCacheUtils.cleanDatabases(getActivity());
			CleanCacheUtils.cleanExternalDatabases(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 从相册获取
	 */
	public void gallery() {
		// 激活系统图库，选择一张图片
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
	}

	@OnPermissionDenied(Manifest.permission.CAMERA)
	void showRecordDenied(){
		Toast.makeText(getActivity(),"拒绝相机权限将无法从相机获取头像",Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		SettingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@OnNeverAskAgain(Manifest.permission.CAMERA)
	void onRecordNeverAskAgain() {
		new AlertDialog.Builder(getActivity())
				.setPositiveButton("好的", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//打开系统设置权限
						Intent intent = getAppDetailSettingIntent(getActivity());
						startActivity(intent);
						dialog.cancel();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(false)
				.setMessage("您已经禁止了相机权限,是否去开启权限")
				.show();
	}

	/**
	 * 获取应用详情页面intent
	 *
	 * @return
	 */
	public static Intent getAppDetailSettingIntent(Context context) {
		Intent localIntent = new Intent();
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT >= 9) {
			localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
		} else if (Build.VERSION.SDK_INT <= 8) {
			localIntent.setAction(Intent.ACTION_VIEW);
			localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
		}
		return localIntent;
	}

	/*
	 * 从相机获取
	 */
	@NeedsPermission(Manifest.permission.CAMERA)
	public void camera() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		// 判断存储卡是否可以用，可用进行存储
		if (hasSdcard()) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), PHOTO_FILE_NAME)));
		}
		startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {
				// 得到图片的全路径
				Uri uri = data.getData();
				crop(uri);
			}
		} else if (requestCode == PHOTO_REQUEST_CAMERA) {
			if(resultCode == Activity.RESULT_OK){
				if (hasSdcard()) {
					tempFile = new File(Environment.getExternalStorageDirectory(),
							PHOTO_FILE_NAME);
					crop(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(getActivity(), "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
				}
			}
		} else if (requestCode == PHOTO_REQUEST_CUT) {
			try {
				bitmap = data.getParcelableExtra("data");
				if (bitmap != null) {
					/**
					 * 上传服务器代码
					 */
					setPicToView(bitmap);// 保存在SD卡中
					this.mHeadPicture.setImageBitmap(bitmap);;// 用ImageView显示出来
//					if (bitmap != null && !bitmap.isRecycled()) {
//						bitmap.recycle();
//					}
				}
				boolean delete = tempFile.delete();
				System.out.println("delete = " + delete);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setPicToView(Bitmap mBitmap) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			return;
		}
		FileOutputStream b = null;
		String fileName = XPlanApplication.getInstance().getExternalFilesDir(null).getAbsolutePath() + "/head.jpg";// 图片名字
		try {
			b = new FileOutputStream(fileName);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 从本地的文件中以保存的图片中 获取图片的方法
	private Bitmap getHead(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}


	/**
	 * 剪切图片
	 * @param uri
	 */
	private void crop(Uri uri) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		// 图片格式
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);// true:不返回uri，false：返回uri
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	private boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
}