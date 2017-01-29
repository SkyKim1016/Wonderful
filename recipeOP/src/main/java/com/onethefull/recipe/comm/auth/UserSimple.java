package com.onethefull.recipe.comm.auth;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.onethefull.recipe.type.FriendshipStatusType;
import com.onethefull.recipe.type.UserGenderType;
import com.onethefull.recipe.type.UserStatusType;

public class UserSimple implements Comparable<UserSimple>,Serializable {
	
	private static final long serialVersionUID = 482679958205176642L;

	private String id;
	private String loginId;
	private String name;
	private String gender;
	private String imageOriginalUrl;
	private String imageThumbUrl;
	private String status;
	private String friendshipStatus;
	private Date regDate;
	private String providerId = "NONE";
	
	public UserSimple() {}	

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getName() {
		return name != null ? name : "회원";
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserGenderType getGender() {
		return UserGenderType.getByCode(gender);
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getImageOriginalUrl() {
		return imageOriginalUrl;
	}

	public void setImageOriginalUrl(String imageOriginalUrl) {
		this.imageOriginalUrl = imageOriginalUrl;
	}

	public String getImageThumbUrl() {
		return imageThumbUrl;
	}

	public void setImageThumbUrl(String imageThumbUrl) {
		this.imageThumbUrl = imageThumbUrl;
	}

	public UserStatusType getStatus() {
		return UserStatusType.getByCode(this.status);
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public FriendshipStatusType getFriendshipStatus() {
		return FriendshipStatusType.getByCode(friendshipStatus);
	}


	public void setFriendshipStatus(String friendshipStatus) {
		this.friendshipStatus = friendshipStatus;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public long getCreateTime() {
		if (regDate != null)
			return regDate.getTime();
		return -1L;
	}	

	public int compareTo(UserSimple o) {
		long time = this.getCreateTime() - o.getCreateTime();
		int diff = 0;
		if (time > 0L) {
			diff = 1;
		} else if (time < 0L) {
			diff = -1;
		}
		return diff;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.id == null)
			return false;
		if (obj instanceof UserSimple) {
			UserSimple other = (UserSimple) obj;
			return new EqualsBuilder().append(getId(), other.getId()).isEquals();
		}
		return false;
	}
	
}
