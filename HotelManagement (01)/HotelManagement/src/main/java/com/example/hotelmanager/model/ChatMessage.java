package com.example.hotelmanager.model;

import jakarta.persistence.Entity;
import lombok.Data;


public class ChatMessage {
	private String type;
	private String content;
	private Object response;
	
	public ChatMessage(String type, String content, Object response) {
		super();
		this.type = type;
		this.content = content;
		this.response = response;
	}

	public ChatMessage() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}
	
	
}
