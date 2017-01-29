package com.onethefull.recipe.req;

import java.util.Date;

import com.onethefull.recipe.comm.req.PageReq;
import com.onethefull.recipe.type.CognizeStatusType;
import com.onethefull.recipe.type.CognizeViewStatusType;

public class CognizeReq extends PageReq {
	private int resultCode;
	private String id;
	private String userId;
	private CognizeStatusType status = CognizeStatusType.defaultStatusType();
	private CognizeViewStatusType viewStatus;
	private Date registDate;
	
	public CognizeReq() {
		super();
	}
	
	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public CognizeStatusType getStatus() {
		return status;
	}

	public void setStatus(CognizeStatusType status) {
		this.status = status;
	}

	public CognizeViewStatusType getViewStatus() {
		return viewStatus;
	}

	public void setViewStatus(CognizeViewStatusType viewStatus) {
		this.viewStatus = viewStatus;
	}

	public Date getRegistDate() {
		return registDate;
	}

	public void setRegistDate(Date registDate) {
		this.registDate = registDate;
	}
}
