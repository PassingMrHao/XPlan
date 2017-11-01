package com.zh.xplan.ui.imagedetail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.base.BaseFragment;
import com.module.common.image.ImageManager;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;

import java.io.Serializable;
import java.util.List;

import static com.zh.xplan.ui.imagedetail.ImageDetailActivity.upDownHideLayout;


/**
 * 显示图片详情的fragment
 */
public class ImageDetailFragment extends BaseFragment {
	private List<GridPictureModel> mPictureModelList;
	private int mPosition;
	private TextView mIntroduction;
	private TextView mPageNumber;
	private PhotoView mPhotoView;
	private ProgressBar mProgressBar;
	private boolean isLoadSuccess = false;

	public static ImageDetailFragment newInstance(List<GridPictureModel> pictureModelList, int position){
		ImageDetailFragment fragment = new ImageDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("pictureModelList", (Serializable) pictureModelList);
		bundle.putInt("position",position);
		fragment.setArguments(bundle);
		return fragment ;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = View.inflate(getActivity().getBaseContext(),
				R.layout.fragment_image_detail, null);
		Bundle bundle = getArguments();
		if(bundle != null){
			this.mPictureModelList = (List<GridPictureModel>) bundle.getSerializable("pictureModelList");
			this.mPosition = bundle.getInt("position");
		}
		initView(mView);
		return mView;
	}

	/**
	 * 初始化views
	 */
	private void initView(View View) {
		mIntroduction  = (TextView) View.findViewById(R.id.introduction);
		mPageNumber  = (TextView) View.findViewById(R.id.tv_page_number);
		mProgressBar  = (ProgressBar) View.findViewById(R.id.pb_progressBar);
		mPhotoView = (PhotoView) View.findViewById(R.id.PhotoView);
		// 6/8 6字体大小为默认的1.3倍
		String text = mPosition + 1 + "/" + mPictureModelList.size() + " " + mPictureModelList.get(mPosition).getPictureTitle();
		int start = text.indexOf("/");
		int end = text.length();
		SpannableString textSpan = new SpannableString (text);
		textSpan.setSpan(new RelativeSizeSpan(1.3f),0,start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textSpan.setSpan(new RelativeSizeSpan(1f),start,end,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		mIntroduction.setText(textSpan);

		ViewCompat.setTransitionName(mPhotoView, mPictureModelList.get(mPosition).getPictureUrl());
		mPhotoView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				LogUtil.e("zh"," mPhotoView.getScale() " + mPhotoView.getScale());
				if(mPhotoView.getScale() !=  1.0f){
					upDownHideLayout.lock();
				}else{
					upDownHideLayout.unLock();
				}
				return mPhotoView.getAttacher().onTouch(v,event);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(! isLoadSuccess){
			ImageManager.displayImage(XPlanApplication.getInstance(),
					mPictureModelList.get(mPosition).getPictureUrl()
					, 0
					, 0
					, new ImageManager.IResult<Bitmap>() {
						@Override
						public void onSuccess(Bitmap result) {
							if(result != null){
								mPhotoView.setImageBitmap(result);
								isLoadSuccess = true;
							}else{
								isLoadSuccess = false;
							}
							mProgressBar.setVisibility(View.GONE);
						}

						@Override
						public void onFail() {
							isLoadSuccess = false;
							mProgressBar.setVisibility(View.GONE);
						}
					});
		}
	}
}