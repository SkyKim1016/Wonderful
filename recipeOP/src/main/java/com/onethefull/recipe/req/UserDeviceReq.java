package com.onethefull.recipe.req;

import com.onethefull.recipe.comm.auth.DeviceStatusType;
import com.onethefull.recipe.comm.auth.DeviceType;

public class UserDeviceReq {
	private String userId;
	private String deviceId;
	private DeviceType deviceType;
	private int resultCode;
	private String resultMessage;
	private DeviceStatusType status = DeviceStatusType.defaultDeviceStatusType();
	
	public UserDeviceReq() {}
	
	public UserDeviceReq(String userId, DeviceType deviceType, String token) {
		this.setUserId(userId);
		this.setDeviceType(deviceType);
		this.setDeviceId(token);
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultMessage() {
		return resultMessage;
	}
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	public DeviceStatusType getStatus() {
		return status;
	}
	public void setStatus(DeviceStatusType status) {
		this.status = status;
	}
}
