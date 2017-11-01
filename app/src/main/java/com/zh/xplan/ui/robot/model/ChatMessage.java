package com.zh.xplan.ui.robot.model;

import java.util.Date;

/**
 * 展示聊天消息的模型
 */
public class ChatMessage {
	private String msg;// 消息内容
	private Type type;// 消息类型，SERVICE 客服消息，CUSTOMER 客户消息
	private Date date;// 日期
	private int state; //消息发送状态   0发送中  1发送成功   -1 发送失败

	public enum Type {
		SERVICE, CUSTOMER
	}

	public ChatMessage() {
	}

	public ChatMessage(String msg, Type type, Date date) {
		this.msg = msg;
		this.type = type;
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
