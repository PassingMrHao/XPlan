package com.zh.xplan.ui.menupicture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.common.log.LogUtil;
import com.module.common.net.HttpManager;
import com.module.common.net.callback.IRequestCallback;
import com.module.common.utils.PixelUtil;
import com.zh.xplan.AppConstant;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.imagedetail.ImageDetailActivity;
import com.zh.xplan.ui.mainactivity.MainActivity;
import com.zh.xplan.ui.menupicture.adapter.GridPictureAdapter;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;
import com.zh.xplan.ui.utils.TitleUtil;
import com.zh.xplan.ui.view.autoscrollviewpager.BGABanner;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;
import com.zh.xplan.ui.view.stateiew.StateView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.qcode.qskinloader.SkinManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import static com.zh.xplan.R.id.recyclerView;

/**
 * 美图美句菜单项
 * Created by zh
 */
public class PictureFragment extends BaseFragment implements OnClickListener{
	private List<GridPictureModel> mPictureModelList;
	private RecyclerView mRecyclerView;
	private GridPictureAdapter mGridPictureAdapter;
	private PullToRefreshLayout mPtrFrame;//下拉刷新
	private int mCurrentPage = 0;// 当前页数
	private Button mToTopBtn;// 返回顶部的按钮
	private StaggeredGridLayoutManager mLayoutManager;

	private List<String> mImageUrl = new ArrayList<String>();
	private String imageUrl1 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4903/41/12296166/85214/15205dd6/58d92373N127109d8.jpg!q70.jpg";
	private String imageUrl2 = "https://img1.360buyimg.com/da/jfs/t4309/113/2596274814/85129/a59c5f5e/58d4762cN72d7dd75.jpg";
	private String imageUrl3 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4675/88/704144946/137165/bbbe8a4e/58d3a160N621fc59c.jpg!q70.jpg";
	private String imageUrl4 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4627/177/812580410/198036/24a79c26/58d4f1e9N5b9fc5ee.jpg!q70.jpg";
	private String imageUrl5 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4282/364/2687292678/87315/e4311cd0/58d4d923N24a2f5eb.jpg!q70.jpg";
	private String imageUrl6 = "https://img1.360buyimg.com/da/jfs/t4162/171/1874609280/92523/a1206b3f/58c7a832Nc8582e81.jpg";

	private StateView mStateView;//加载状态控件
	private View mStateViewRetry;//错误状态布局的根布局

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = View.inflate(getActivity(),
				R.layout.fragment_pic, null);
		initTitle(getActivity(), mView);
		initView(mView);
		initData();
		return mView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SkinManager.getInstance().applySkin(view, true);
	}

	/**
	 * 初始化标题栏
	 * @param activity
	 * @param view
	 */
	private void initTitle(Activity activity, View view) {
		// 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		new TitleUtil(activity, view).setLeftImageRes(R.drawable.title_bar_menu, this)
				.setMiddleTitleText("美图美句")
				.setMiddleTitleBgColor(getActivity().getResources().getColor(R.color.white))
				.setRightImageRes(R.drawable.scan_barcode, this);
	}

	/**
	 * 初始化界面
	 * @param rootView
	 */
	private void initView(View rootView) {
		initPtrFrame(rootView);
		mStateView = (StateView) rootView.findViewById(R.id.mStateView);
		mStateViewRetry  =  rootView.findViewById(R.id.ll_stateview_error);
		mStateViewRetry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStateView.setCurrentState(StateView.STATE_LOADING);
				mCurrentPage = 0;
				updateData(mCurrentPage,true);
			}
		});

		mToTopBtn = (Button) rootView.findViewById(R.id.btn_top);
		mToTopBtn.setOnClickListener(this);
		mRecyclerView = (RecyclerView) rootView.findViewById(recyclerView);
		mLayoutManager = new StaggeredGridLayoutManager(
				2, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addItemDecoration(new SpacesItemDecoration(PixelUtil.dp2px(3,getContext())));
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int[] fistVisibleItem = mLayoutManager.findFirstVisibleItemPositions(new int[2]);
				// 判断是否滚动超过一屏
				if (0 == fistVisibleItem[0]) {
					mToTopBtn.setVisibility(View.GONE);
				} else {
					mToTopBtn.setVisibility(View.VISIBLE);
				}
			}

//			@Override
//			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//					ImageManager.resume();
//				}else {
//					ImageManager.pause();
//				}
//			}
		});
	}

	public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
            int spanCount = layoutManager.getSpanCount();
            if( spanCount == 2 &&  parent.getChildAdapterPosition(view) != 0){
				int index = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
				if( index % 2  == 0){
					outRect.right = space/2;
				}else{
					outRect.left = space/2;
				}
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }
	}

	/**
	 * 初始化下拉刷新
	 */
	private void initPtrFrame(View rootView) {
		mPtrFrame = (PullToRefreshLayout) rootView.findViewById(R.id.rotate_header_list_view_frame);
		mPtrFrame.setPtrHandler(new PtrHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				updateData(0,true);
			}
		});
	}

	private void initData() {
		//设置加载状态为加载中
		mStateView.setCurrentState(StateView.STATE_LOADING);

		mImageUrl.add(imageUrl1);
		mImageUrl.add(imageUrl2);
		mImageUrl.add(imageUrl3);
		mImageUrl.add(imageUrl4);
		mImageUrl.add(imageUrl5);
		mImageUrl.add(imageUrl6);
		View headView = LayoutInflater.from(getActivity()).inflate(
				R.layout.homerecycle_item_top_banner, null);
		BGABanner banner = (BGABanner) headView.findViewById(R.id.banner);
		banner.setDelegate(new BGABanner.Delegate<View, String>() {
			@Override
			public void onBannerItemClick(BGABanner banner, View itemView, String imageUrl, int position) {
//                Toast.makeText(itemView.getContext(), "" + imageUrl, Toast.LENGTH_SHORT).show();
			}
		});
		banner.setAdapter(new BGABanner.Adapter<View, String>() {
			@Override
			public void fillBannerItem(BGABanner banner, View itemView, String imageUrl, int position) {
				SkinManager.getInstance().applySkin(itemView, true);
				SimpleDraweeView simpleDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_item_fresco_content);
				simpleDraweeView.setImageURI(Uri.parse(imageUrl));
			}
		});
		banner.setData(R.layout.homerecycle_top_banner_content,mImageUrl, null);

		mPictureModelList = new ArrayList();
		mGridPictureAdapter = new GridPictureAdapter(mPictureModelList);
		mGridPictureAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				updateData(mCurrentPage,false);
			}
		}, mRecyclerView);
		mGridPictureAdapter.setEnableLoadMore(true);
		mGridPictureAdapter.setHeaderView(headView);
		mGridPictureAdapter.setOnItemClickLitener(new GridPictureAdapter.OnItemClickLitener() {
			@Override
			public void onItemClick(View view, int position) {
				LogUtil.e("zh","mGridPictureAdapter setOnItemClickLitener");
				Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
				intent.putExtra("mPictureModelList", (Serializable) mPictureModelList);
				intent.putExtra("position", position);
				  /*if (Build.VERSION.SDK_INT >= 21) {
//                	ActivityOptionsCompat options = ActivityOptionsCompat.
//                		makeScaleUpAnimation(view,
//                                     (int)view.getWidth()/2, (int)view.getHeight()/2, //拉伸开始的坐标
//                                     0, 0);//拉伸开始的区域大小，这里用（0，0）表示从无到全屏
                	ActivityOptionsCompat options = ActivityOptionsCompat
                          .makeSceneTransitionAnimation(getActivity(), view, mPictureModelList.get(position).getPictureUrl());
                	ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
              } else {
            	  	getActivity().startActivity(intent);
//            	  	getActivity().overridePendingTransition(-1, -1);
              }*/
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(0,0);
			}
		});
		mGridPictureAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
			@Override
			public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
				LogUtil.e("zh","mGridPictureAdapter setOnItemChildClickListener");
			}
		});
		mRecyclerView.setAdapter(mGridPictureAdapter);
		updateData(mCurrentPage,true);
	}

	private void updateData(int currentPage, final Boolean isPullDownRefresh) {
		HttpManager.builder()
				.url(AppConstant.MEI_TU_MEI_JU_URL)
				.params("page",currentPage)
				.requestCallbak(new IRequestCallback() {

					@Override
					public void onSuccess(String response) {
						Document mDocument = Jsoup.parse(response);
						List<String> titleList = new ArrayList<>();
						Elements es = mDocument.getElementsByClass("xlistju");
						for (Element e : es) {
							titleList.add(e.text());
						}
						List<String> hrefList = new ArrayList<>();
						List<Integer> heightList = new ArrayList<>();
						List<Integer> widthList = new ArrayList<>();
						Elements es1 = mDocument.getElementsByClass("chromeimg");
						for (Element e : es1) {
							hrefList.add(e.attr("src"));
							if (null == e.attr("width") || "".equals(e.attr("width"))) {
								widthList.add(300); //没有返回宽度时，设置默认高度300
							} else {
								widthList.add(Integer.parseInt((e.attr("width").substring(0, e.attr("width").length() - 2))));
							}
							if (null == e.attr("height") || "".equals(e.attr("height"))) {
								heightList.add(200); //没有返回高度时 设置默认高度200
							} else {
								heightList.add(Integer.parseInt((e.attr("height").substring(0, e.attr("height").length() - 2))));
							}
						}
						List<GridPictureModel> pictureModelList = new ArrayList<>();
						for (int i = 0; i < hrefList.size(); i++) {
							GridPictureModel pictureModel = new GridPictureModel();
							pictureModel.setPictureTitle(titleList.get(i));
							pictureModel.setPictureUrl("http:" + hrefList.get(i));
							pictureModel.setPictureHeight(heightList.get(i));
							pictureModel.setPictureWidth(widthList.get(i));
							pictureModelList.add(pictureModel);
						}

						if (pictureModelList != null) {
							mStateView.setCurrentState(StateView.STATE_CONTENT);
							mCurrentPage++;
							if (isPullDownRefresh) {
								// 下拉刷新，重新刷新列表
								mPictureModelList.clear();
								mPictureModelList.addAll(pictureModelList);
								mCurrentPage = 1;
								mPtrFrame.refreshComplete();
								mGridPictureAdapter.notifyDataSetChanged();
							} else {
								mPictureModelList.addAll(pictureModelList);
								mGridPictureAdapter.loadMoreComplete();
							}
//					if(isFirstLoadData){
//						System.out.println("zhzhz:: setupViews();");
//						setupViews();
//						isFirstLoadData = false;
//					}
						}
					}

					@Override
					public void onError(int code, String msg) {
						LogUtil.e("zh", "onFailure: " + msg.toString());
						if (mPictureModelList.isEmpty()) {
							mStateView.setCurrentState(StateView.STATE_ERROR);
						} else {
							Toast.makeText(getActivity(), "数据请求失败", Toast.LENGTH_SHORT).show();
						}
						if (isPullDownRefresh) {
							mPtrFrame.refreshComplete();
						} else {
							mGridPictureAdapter.loadMoreFail();
						}
					}
				})
				.build()
				.get();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_left_imageview:// 打开或关闭侧滑菜单
				((MainActivity)getActivity()).openDrawer();
				break;
			case R.id.title_right_imageview:// 扫一扫
				break;
			case R.id.btn_top://快速返回顶部
				mRecyclerView.scrollToPosition(0);
//				mRecyclerView.smoothScrollToPosition(0);
				break;
			default:
				break;
		}
	}
}