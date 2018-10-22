package com.full.feeds.model;

import java.util.Date;

public class Feed {
	
	private String id;
	private String feedValue;
	private String createdTime;
	private String userId;
	private boolean status;
	
	private Date date;
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFeedValue() {
		return feedValue;
	}

	public void setFeedValue(String feedValue) {
		this.feedValue = feedValue;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
