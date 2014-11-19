package com.example.communityanimator.database;

public class User {

	private String name;
	private boolean status;
	private boolean chatting;
	private byte[] file;

	public String getUsername() {
		return name;
	}

	public void setUsername(String value) {
		this.name = value;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean value) {
		this.status = value;
	}

	public Boolean getChatting() {
		return chatting;
	}

	public void setChatting(Boolean value) {
		this.chatting = value;
	}

	public byte[] getPhotoFile() {
		return file;
	}

	public void setPhotoFile(byte[] value) {
		this.file = value;
	}

}
