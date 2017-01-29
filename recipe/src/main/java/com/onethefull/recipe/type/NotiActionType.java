package com.onethefull.recipe.type;

public enum NotiActionType {
	NONE(""), FRIENDSHIPREQUEST("FR"), FRIENDSHIPRECEIVEREQUEST("FV"), FRIENDSHIPACCEPT("FF"), FRIENDSHIPREQUESTCANCELED("FC"), FRIENDSHIPREQUESTREJECTED("FJ"), FRIENDSHIPCANCELED("FD"), COGNIZED("CT") ;
	
	private String code;
	
	NotiActionType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static NotiActionType getByCode(String val) {
		for(NotiActionType e:NotiActionType.values()) {
			if(e.getCode().equals(val)) {
				return e;
			}
		}
		return NotiActionType.defaultNotiActionType();
	}
	
	public static NotiActionType defaultNotiActionType() {
		return NotiActionType.NONE;
	}
}
