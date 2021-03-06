package com.onethefull.recipe.type;

public enum FriendshipStatusType {
	NONE(""), ONREQUESTING("FR"), RECEIVEREQEUST("FV"), FRIEND("FF"), REQUESTCANCELED("FC"), REQUESTREJECTED("FJ"), FRIENDSHIPCANCELED("FD") ;
	
	private String code;
	
	FriendshipStatusType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static FriendshipStatusType getByCode(String val) {
		for(FriendshipStatusType e:FriendshipStatusType.values()) {
			if(e.getCode().equals(val)) {
				return e;
			}
		}
		return FriendshipStatusType.defaultFriendshipStatusType();
	}
	
	public static FriendshipStatusType defaultFriendshipStatusType() {
		return FriendshipStatusType.NONE;
	}
}
