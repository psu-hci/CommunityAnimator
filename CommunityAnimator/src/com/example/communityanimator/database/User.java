package com.example.communityanimator.database;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("_User")
public class User extends ParseObject {

	public User() {
		// A default constructor is required.
	}

	public ParseUser getUser() {
		return getParseUser("_User");
	}

	public void setUser(ParseUser value) {
		put("_User", value);
	}

	public Boolean getStatus() {
		return getBoolean("status");
	}

	public void setStatus(Boolean value) {
		put("status", value);
	}

	public void setGender(String value) {
		put("gender", value);
	}

	public String getGender() {
		return getString("gender");
	}

	public void setOccupation(String value) {
		put("occupation", value);
	}

	public String getOccupation() {
		return getString("occupation");
	}

	public Boolean getReminder() {
		return getBoolean("reminder");
	}

	public void setReminder(Boolean value) {
		put("reminder", value);
	}

	public Boolean getView() {
		return getBoolean("view");
	}

	public void setView(Boolean value) {
		put("view", value);
	}

	public ParseGeoPoint getLocation() {
		return getParseGeoPoint("location");
	}

	public void setLocation(ParseGeoPoint value) {
		put("location", value);
	}

	public ParseFile getPhotoFile() {
		return getParseFile("image");
	}

	public void setPhotoFile(ParseFile file) {
		put("image", file);
	}

	public static ParseQuery<User> getQuery() {
		return ParseQuery.getQuery(User.class);
	}

}
