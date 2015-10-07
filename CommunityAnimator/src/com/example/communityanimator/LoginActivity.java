package com.example.communityanimator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.communityanimator.message.MessageService;
import com.example.communityanimator.util.Application;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	private Dialog progressDialog;
	Button btn_LoginIn = null;
	Button btn_SignUp = null;
	Button btn_twitter = null;
	TextView btn_ForgetPass = null;
	CheckBox reminder = null;

	private EditText mUserNameEditText;
	private EditText mPasswordEditText;

	// flag for Internet connection status
	Boolean isInternetPresent = false;
	// Connection detector class
	ConnectionDetector cd;
	private Intent serviceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		cd = new ConnectionDetector(getApplicationContext());

		serviceIntent = new Intent(getApplicationContext(),
				MessageService.class);

		btn_LoginIn = (Button) findViewById(R.id.btn_login);
		btn_SignUp = (Button) findViewById(R.id.btn_signup);
		btn_twitter = (Button) findViewById(R.id.btn_twitter);
		btn_ForgetPass = (TextView) findViewById(R.id.forgot);
		mUserNameEditText = (EditText) findViewById(R.id.username);
		mPasswordEditText = (EditText) findViewById(R.id.password);
		reminder = (CheckBox) findViewById(R.id.remembercheck);

		// Check if there is a currently logged in user
		// and it's linked to a Facebook account.
		ParseUser currentUser = ParseUser.getCurrentUser();
		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			showUserDetailsActivity();
		}

		btn_LoginIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// get Internet status
				isInternetPresent = cd.isConnectingToInternet();
				// check for Internet status
				if (isInternetPresent) {
					// Internet Connection is Present
					// make HTTP requests
					attemptLogin();
				} else {
					// Internet connection is not present
					// Ask user to connect to Internet
					showAlertDialog(LoginActivity.this,
							"No Internet Connection",
							"You don't have internet connection.", false);
				}

			}
		});

		btn_SignUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, SignUp.class);
				startActivity(intent);
			}
		});

		btn_ForgetPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent in = new Intent(LoginActivity.this,
						ForgetParsePassword.class);
				startActivity(in);
			}
		});

		btn_twitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent in = new Intent(LoginActivity.this, TwitterLogin.class);
				startActivity(in);
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
	}

	public void onLoginClick(View v) {
		progressDialog = ProgressDialog.show(LoginActivity.this, "",
				"Logging in...", true);

		List<String> permissions = Arrays.asList("public_profile", "email");
		// NOTE: for extended permissions, like "user_about_me", your app must
		// be reviewed by the Facebook team
		// (https://developers.facebook.com/docs/facebook-login/permissions/)

		ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
				permissions, new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException err) {
						progressDialog.dismiss();
						if (user == null) {
							Log.d(Application.APPTAG,
									"Uh oh. The user cancelled the Facebook login.");
						} else if (user.isNew()) {
							Log.d(Application.APPTAG,
									"User signed up and logged in through Facebook!");
							showUserDetailsActivity();
						} else {
							Log.d(Application.APPTAG,
									"User logged in through Facebook!");
							showUserDetailsActivity();
						}
					}
				});
	}

	private void showUserDetailsActivity() {
		Intent intent = new Intent(this, SignUp.class);
		startActivity(intent);
	}

	public void onCreateParse() {
		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
	}

	public void attemptLogin() {

		clearErrors();

		// Store values at the time of the login attempt.
		String username = mUserNameEditText.getText().toString();
		String password = mPasswordEditText.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid username and password.
		if (TextUtils.isEmpty(username)) {
			mUserNameEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = mUserNameEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(password)) {
			mPasswordEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = mPasswordEditText;
			cancel = true;
		} else if (password.length() < 4) {
			mPasswordEditText
					.setError(Html
							.fromHtml("<font color='red'>This password must be at least 5 characters long.</font>"));
			focusView = mPasswordEditText;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// perform the user login attempt.
			login(username.toLowerCase(Locale.getDefault()), password);
		}
	}

	private void login(String lowerCase, String password) {

		// Set up a progress dialog
		final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
		dialog.setMessage(getString(R.string.login_progress_signing_in));
		dialog.show();

		ParseUser.logInInBackground(lowerCase, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				dialog.dismiss();
				if (e == null) {
					startService(serviceIntent);
					loginSuccessful();
					// Associate the device with a user
					ParseInstallation installation = ParseInstallation
							.getCurrentInstallation();
					installation.put("user", user.getUsername());
					installation.saveInBackground();
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
					loginUnSuccessful();
				}

			}
		});

	}

	protected void loginSuccessful() {

		if (reminder.isChecked()) {
			Log.d(Application.APPTAG, "reminder check!");
			ParseUser user = ParseUser.getCurrentUser();
			user.put("reminder", true);
			user.saveInBackground();
		}

		// Start an intent for the dispatch activity
		Intent intent = new Intent(LoginActivity.this, DispatchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	protected void loginUnSuccessful() {
		showAlertDialog(LoginActivity.this, "Login",
				"Username or Password is invalid.", false);
	}

	private void clearErrors() {
		mUserNameEditText.setError(null);
		mPasswordEditText.setError(null);
	}

	@SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

}
