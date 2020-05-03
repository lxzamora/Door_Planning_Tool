package com.xpo.doorplanningtool.cnst;


public enum EmailConstants {
	
	SYSTEM_EMAIL				("bisubscriptions@xpo.com"),
	SYSTEM_EMAIL_PASSWORD		(""),
	SYSTEM_EMAIL_HOST			("mailhost.con-way.com"),
	SYSTEM_EMAIL_PORT			(""),
	SYSTEM_EMAIL_TO_RECIPIENTS	("liza.zamora@xpo.com"),
	SYSTEM_EMAIL_CC_RECIPIENTS	("bisubscriptions@xpo.com"),
	SYSTEM_EMAIL_BCC_RECIPIENTS	(""),
	SYSTEM_EMAIL_MESSAGE_BODY	("The door planning tool is also available at O:\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\n"),
	;
	
	String value = null;
	private EmailConstants(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
