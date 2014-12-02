package com.example.communityanimator.scenarios;

import java.util.ArrayList;

public class InterestParent<child> {
	private String text;
	private String checkedtype;
	private boolean checked;

	// ArrayList to store child objects
	private ArrayList<child> children;

	public String getText() {
		return text;
	}

	public void setText(String text1) {
		this.text = text1;
	}

	public String getCheckedType() {
		return checkedtype;
	}

	public void setCheckedType(String checkedtype) {
		this.checkedtype = checkedtype;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	// ArrayList to store child objects
	public ArrayList<child> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<child> children) {
		this.children = children;
	}

}
