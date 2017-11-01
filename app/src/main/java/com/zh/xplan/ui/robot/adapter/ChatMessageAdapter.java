package com.zh.xplan.ui.robot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zh.xplan.R;
import com.zh.xplan.ui.robot.listener.ReSendMsgLinsener;
import com.zh.xplan.ui.robot.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 智能客服聊天信息显示适配器
 * 2016-1-8
 */
public class ChatMessageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<ChatMessage> mMsgList;
	private Context context;
	private ReSendMsgLinsener resendMsgLinsener;
	
	public void setResendMsgLinsener(ReSendMsgLinsener resendMsgLinsener) {
		this.resendMsgLinsener = resendMsgLinsener;
	}

	public ChatMessageAdapter(Context context, List<ChatMessage> mMsgList) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.mMsgList = mMsgList;
	}
	
	@Override
	public int getCount() {
		return mMsgList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMsgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ChatMessage chatMessage = mMsgList.get(position);
		ViewHolder viewHolder = null;
		if(convertView == null) {
			//根据ItemType设置不同的布局    0表示客服  1表示用户
			viewHolder = new ViewHolder();
			if(getItemViewType(position) == 0) {
				convertView = mInflater.inflate(R.layout.robot_serivce_msg, parent, false);
			} else {
				convertView = mInflater.inflate(R.layout.robot_customer_msg, parent, false);
				viewHolder.mRetry =  (TextView) convertView.findViewById(R.id.tv_retry);
				viewHolder.mProgress =  (ProgressBar) convertView.findViewById(R.id.pb_progress);
			}
			viewHolder.mDate =  (TextView) convertView.findViewById(R.id.tv_msg_date);
			viewHolder.mMsg = (TextView) convertView.findViewById(R.id.tv_msg_info);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//显示数据
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		viewHolder.mDate.setText(df.format(chatMessage.getDate()));
		viewHolder.mMsg.setText(chatMessage.getMsg());
		if(getItemViewType(position) == 1){
			
			if(chatMessage.getState() == 0){
				//发送中
				viewHolder.mRetry.setVisibility(View.INVISIBLE);
				viewHolder.mProgress.setVisibility(View.VISIBLE);
			}else if(chatMessage.getState() == 1){
				//发送成功
				viewHolder.mRetry.setVisibility(View.GONE);
				viewHolder.mProgress.setVisibility(View.GONE);
			}else if(chatMessage.getState() == -1){
				//发送失败
				viewHolder.mProgress.setVisibility(View.GONE);
				viewHolder.mRetry.setVisibility(View.VISIBLE);
			}
			
			viewHolder.mRetry.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					resendMsgLinsener.onResendMsg(position);
				}
			});
		}
		return convertView;
	}

	//两种不同的item
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		if(mMsgList.get(position).getType() == ChatMessage.Type.SERVICE) {
			return 0;
		}
		return 1;
	}
	
	private static  class ViewHolder {
		TextView mDate;
		TextView mMsg;
		TextView mRetry;
		ProgressBar mProgress;
	}
}
