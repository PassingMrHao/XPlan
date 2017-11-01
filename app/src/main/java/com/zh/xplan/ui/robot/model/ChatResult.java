package com.zh.xplan.ui.robot.model;


/**
 * 用于接收服务器返回结果的模型
 * @author zh
 */
public class ChatResult {
	private int code;// 状态码
	private String text;// 消息内容

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
