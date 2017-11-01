package com.zh.xplan.ui.menupicture.adapter;

import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zh.xplan.R;
import com.module.common.utils.PixelUtil;
import com.module.common.image.ImageManager;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;

import org.qcode.qskinloader.SkinManager;
import java.util.List;

/**
 * 瀑布流图片适配器
 */
public class GridPictureAdapter extends BaseMultiItemQuickAdapter<GridPictureModel, BaseViewHolder> implements BaseQuickAdapter.SpanSizeLookup{
    private OnItemClickLitener mOnItemClickLitener;

    public GridPictureAdapter(List<GridPictureModel> data){
        super(data);
        setSpanSizeLookup(this);
        addItemType(0, R.layout.item_simple_textview);
    }

    @Override
    protected void convert(BaseViewHolder helper, GridPictureModel item) {
        bindListData(helper, item);
    }

    @Override
    public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
        return 0;
    }

    private void bindListData(final BaseViewHolder helper, GridPictureModel item) {
        SkinManager.with(helper.itemView).applySkin(true);

        helper.itemView.setClickable(true);
        helper.setText(R.id.id_tv, item.getPictureTitle());

        SimpleDraweeView simpleDraweeView = helper.getView(R.id.iv_image);
//        simpleDraweeView.setImageURI(Uri.parse(item.getPictureUrl()));
        ImageManager.displayImage(simpleDraweeView,Uri.parse(item.getPictureUrl()),PixelUtil.dp2px(50,mContext),PixelUtil.dp2px(50,mContext));
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null){
                    mOnItemClickLitener.onItemClick(v, helper.getLayoutPosition() - 1 );
                }
            }
        });

    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}
