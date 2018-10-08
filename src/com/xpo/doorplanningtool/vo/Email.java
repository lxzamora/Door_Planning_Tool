package com.xpo.doorplanningtool.vo;

import com.xpo.doorplanningtool.cnst.EmailConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Email class used for the notifications from the feedback mechanism
 * 
 * @author mku
 */
public class Email {

	String sender; // email address associated with the Data Dictionary Tool

	// recipient email address (Data Dictionary Tool Admins)
	List<String> toRecipients = new ArrayList<String>(); // TO recipients
	List<String> ccRecipients = new ArrayList<String>(); // CC recipients
	List<String> bccRecipients = new ArrayList<String>(); // BCC recipients

	String subject; // email subject
	String message; // email message
	
	public Email() {
		setSender(EmailConstants.SYSTEM_EMAIL.getValue());
		
		String[] toRcps = EmailConstants.SYSTEM_EMAIL_TO_RECIPIENTS.getValue().split(",");
		if (toRcps.length > 0){
			for (int i = 0; i < toRcps.length; i++){
				toRecipients.add(toRcps[i]);
			}
		} else {
			if (EmailConstants.SYSTEM_EMAIL_TO_RECIPIENTS.getValue().length() > 0){
				toRecipients.add(EmailConstants.SYSTEM_EMAIL_TO_RECIPIENTS.getValue());
			}
		}
		
		String[] ccRcps = EmailConstants.SYSTEM_EMAIL_CC_RECIPIENTS.getValue().split(",");
		if (ccRcps.length > 0){
			for (int i = 0; i < ccRcps.length; i++){
				ccRecipients.add(ccRcps[i]);
			}
		} else {
			if (EmailConstants.SYSTEM_EMAIL_CC_RECIPIENTS.getValue().length() > 0){
				ccRecipients.add(EmailConstants.SYSTEM_EMAIL_CC_RECIPIENTS.getValue());
			}
		}
		
		String[] bccRcps = EmailConstants.SYSTEM_EMAIL_BCC_RECIPIENTS.getValue().split(",");
		if (bccRcps.length > 0){
			for (int i = 0; i < bccRcps.length; i++){
				bccRecipients.add(bccRcps[i]);
			}
		} else {
			if (EmailConstants.SYSTEM_EMAIL_BCC_RECIPIENTS.getValue().length() > 0){
				bccRecipients.add(EmailConstants.SYSTEM_EMAIL_BCC_RECIPIENTS.getValue());
			}
		}
		
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public List<String> getToRecipients() {
		return toRecipients;
	}

	public void setToRecipients(List<String> toRecipients) {
		this.toRecipients = toRecipients;
	}

	public List<String> getCcRecipients() {
		return ccRecipients;
	}

	public void setCcRecipients(List<String> ccRecipients) {
		this.ccRecipients = ccRecipients;
	}

	public List<String> getBccRecipients() {
		return bccRecipients;
	}

	public void setBccRecipients(List<String> bccRecipients) {
		this.bccRecipients = bccRecipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}