package com.zh.xplan.ui.robot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.module.common.log.LogUtil;
import com.module.common.net.HttpManager;
import com.module.common.net.callback.IRequestCallback;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.robot.adapter.ChatMessageAdapter;
import com.zh.xplan.ui.robot.listener.ReSendMsgLinsener;
import com.zh.xplan.ui.robot.model.ChatMessage;
import com.zh.xplan.ui.robot.model.ChatResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 小机器人界面
 * @author zh 2015-12-1 下午9:34:21
 */
public class RobotActivity extends BaseActivity  implements View.OnClickListener{
	private ListView mMsgListView;
	private EditText mInputEditText;
	private Button mSendBtn;	
	private ChatMessageAdapter mAdapter;
	private List<ChatMessage> mMsgList;
	private Gson mGson = new Gson();

	// 接入图灵平台所需url和key
	private static final String URL = " http://www.tuling123.com/openapi/api";
	private static final String API_KEY = "180bc6fe1df26611c2259c2f91dee61a";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_robot);
		initView();
		initDatas();
		initListener();
	}

	private void initView() {
		findViewById(R.id.title_bar_back).setOnClickListener(this);
//		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);  和android:fitsSystemWindows="true" 有冲突
		mMsgListView = (ListView) findViewById(R.id.lv_listview_msgs);
		mInputEditText = (EditText) findViewById(R.id.et_input);
		mSendBtn = (Button) findViewById(R.id.btn_send);
		mSendBtn.setEnabled(false);
	}
	
	private void initDatas() {
		mMsgList = new ArrayList<ChatMessage>();
		mMsgList.add(new ChatMessage("你好，我是小机器人，想和我说什么呢？", ChatMessage.Type.SERVICE, new Date()));
		mAdapter = new ChatMessageAdapter(this, mMsgList);
		mMsgListView.setAdapter(mAdapter);		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_bar_back:
				finish();
				break;
			default:
				break;
		}
	}
	
	private void initListener() {
		ReSendMsgLinsener resendMsgLinsener = new ReSendMsgLinsener() {
			@Override
			public void onResendMsg(int position) {				
				ResendMessage(position);
			}
		};
		mAdapter.setResendMsgLinsener(resendMsgLinsener);
		
		mMsgListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyBroad();
				return false;
			}
		});
		mInputEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(TextUtils.isEmpty(s)){
					mSendBtn.setEnabled(false);
				}else{
					mSendBtn.setEnabled(true);
				}
			}
		});
		mSendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String messageStr = mInputEditText.getText().toString().trim();
				SendMessage(messageStr);
			}
		});
	}
	
	public void ResendMessage(int position){		
		if (isNetConnected(getApplicationContext()) == false) {
			Toast.makeText(getApplicationContext(), "网络连接失败，请稍后再试！", Toast.LENGTH_SHORT).show();
			return;
		}
		String messageStr = mMsgList.get(position).getMsg();
		mMsgList.remove(position);
		mAdapter.notifyDataSetChanged();
		SendMessage(messageStr);
	}
	
	public void SendMessage(final String messageStr){
		if (TextUtils.isEmpty(messageStr)) {
			return;
		}
		if (isNetConnected(getApplicationContext()) == false) {
//			Toast.makeText(getApplicationContext(), "网络连接失败，请确认网络连接", Toast.LENGTH_SHORT).show();
			ChatMessage customerMessage = new ChatMessage();
			customerMessage.setDate(new Date());
			customerMessage.setMsg(messageStr);
			customerMessage.setType(ChatMessage.Type.CUSTOMER);
			customerMessage.setState(-1);
			mMsgList.add(customerMessage);
			mAdapter.notifyDataSetChanged();
			mMsgListView.setSelection(mMsgList.size() - 1);
			mInputEditText.setText("");
			return;
		}		
		
		final ChatMessage customerMessage = new ChatMessage();
		customerMessage.setDate(new Date());
		customerMessage.setMsg(messageStr);
		customerMessage.setType(ChatMessage.Type.CUSTOMER);
		customerMessage.setState(0);
		mMsgList.add(customerMessage);
		mAdapter.notifyDataSetChanged();
		mMsgListView.setSelection(mMsgList.size() - 1);
		mInputEditText.setText("");
		sendMessage(messageStr, new IRequestCallback() {
			@Override
			public void onSuccess(String response) {
				String jsonRes = response;
				LogUtil.e(TAG,"sendMessage  onSuccess" + jsonRes);

				if (!jsonRes.equals("")) {
					ChatResult result = null;
					try {
						result = mGson.fromJson(jsonRes, ChatResult.class);
						ChatMessage chatMessage = new ChatMessage();
						chatMessage.setMsg(result.getText());
						chatMessage.setDate(new Date());
						chatMessage.setType(ChatMessage.Type.SERVICE);
						//发送成功
						customerMessage.setState(1);
						mMsgList.add(chatMessage);
						mAdapter.notifyDataSetChanged();
						mMsgListView.setSelection(mMsgList.size() - 1);
						return;
					} catch (JsonSyntaxException e) {
						e.printStackTrace();
					}
				}
				//发送失败
				customerMessage.setState(-1);
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(int code, String msg) {
				//发送失败
				customerMessage.setState(-1);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	public boolean isNetConnected(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void hideKeyBroad() {
		if (getCurrentFocus() != null
				&& getCurrentFocus().getWindowToken() != null) {
			InputMethodManager imInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imInputMethodManager != null) {
				imInputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	/**
	 * 获取到请求地址
	 * @param msg
	 * @return
	 */
	private String getUrl(String msg) {
		String url = "";
		try {
			url = URL + "?key=" + API_KEY + "&info="
					+ URLEncoder.encode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public void sendMessage(String msg, IRequestCallback iRequestCallback) {
		// 得到json格式的结果
		String url = getUrl(msg);
		HttpManager.builder()
				.url(url)
				.requestCallbak(iRequestCallback)
				.build()
				.get();
	}

}
