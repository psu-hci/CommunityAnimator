package com.example.communityanimator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.example.communityanimator.message.MessageService;
import com.example.communityanimator.util.ConfigHelper;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Activity which starts an intent for either the logged in (MainActivity) or
 * logged out (SignUpOrLoginActivity) activity.
 */
public class DispatchActivity extends Activity {

	public DispatchActivity() {
	}

	private Intent serviceIntent;
	// flag for Internet connection status
	Boolean isInternetPresent = false;
	// Connection detector class
	ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Clear previous Preference
		ConfigHelper.clearPreferences(getApplicationContext());

		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());

		serviceIntent = new Intent(getApplicationContext(),
				MessageService.class);

		// get Internet status
		isInternetPresent = cd.isConnectingToInternet();
		// check for Internet status
		if (isInternetPresent) {

			// Check if there is current user info
			if (ParseUser.getCurrentUser() != null) {

				startService(serviceIntent);
				// Associate the device with a user
				ParseInstallation installation = ParseInstallation
						.getCurrentInstallation();
				installation.put("user", ParseUser.getCurrentUser()
						.getUsername());
				installation.saveInBackground();

				Object reminder = ParseUser.getCurrentUser().getBoolean(
						"reminder");

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
		} else {
			// Internet connection is not present
			// Ask user to connect to Internet
			showAlertDialog(DispatchActivity.this, "No Internet Connection",
					"You don't have internet connection.", false);
		}

	}

	@SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	private void findScreenSize() {
		int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		switch (screenSize) {
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			Toast.makeText(this, "Large screen", Toast.LENGTH_LONG).show();
			break;
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			Toast.makeText(this, "Normal screen", Toast.LENGTH_LONG).show();
			break;
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			Toast.makeText(this, "Small screen", Toast.LENGTH_LONG).show();
			break;
		default:
			Toast.makeText(this,
					"Screen size is neither large, normal or small",
					Toast.LENGTH_LONG).show();

		}
	}

	private void findDensity() {
		int density = getResources().getDisplayMetrics().densityDpi;
		switch (density) {
		case DisplayMetrics.DENSITY_LOW:
			Toast.makeText(getApplicationContext(), "LDPI", Toast.LENGTH_SHORT)
					.show();
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			Toast.makeText(getApplicationContext(), "MDPI", Toast.LENGTH_SHORT)
					.show();
			break;
		case DisplayMetrics.DENSITY_HIGH:
			Toast.makeText(getApplicationContext(), "HDPI", Toast.LENGTH_SHORT)
					.show();
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			Toast.makeText(getApplicationContext(), "XHDPI", Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}
}
