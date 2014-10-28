package com.example.communityanimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

/**
 * Activity which starts an intent for either the logged in (MainActivity) or
 * logged out (SignUpOrLoginActivity) activity.
 */
public class DispatchActivity extends Activity {

	public DispatchActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check if there is current user info
		if (ParseUser.getCurrentUser() != null) {
			Object reminder = ParseUser.getCurrentUser().getBoolean("reminder");
			// Start an intent for the logged in activity
			if (reminder.equals(true)) {
				startActivity(new Intent(this, MainActivity.class));
			} else {
				startActivity(new Intent(this, Login.class));
			}

		} else {
			// Start and intent for the logged out activity
			startActivity(new Intent(this, Login.class));
		}
	}

}
