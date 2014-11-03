package com.example.communityanimator;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class FacebookLogin extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Initializing Parse SDK
		onCreateParse();
		// Calling ParseAnalytics to see Analytics of our app
		// ParseAnalytics.trackAppOpened(getIntent());
		onLoginButtonClicked();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	private void onLoginButtonClicked() {

		List<String> permissions = Arrays.asList("public_profile", "user_name",
				"user_gender", "user_email", "user_birthday", "user_location");
		ParseFacebookUtils.logIn(permissions, FacebookLogin.this,
				new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException err) {
						if (user != null) {
							if (user.isNew()) {
								// set favorites as null, or mark it as empty
								// somehow
								makeMeRequest();
							} else {
								finishActivity();
							}
						}
					}

				});
	}

	private void makeMeRequest() {

		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			Request request = Request.newMeRequest(
					ParseFacebookUtils.getSession(),
					new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							if (user != null) {
								ParseUser.getCurrentUser().put("firstName",
										user.getUsername());
								ParseUser.getCurrentUser().saveInBackground();
								finishActivity();
							} else if (response.getError() != null) {
								if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
										|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
									Toast.makeText(getApplicationContext(),
											R.string.session_invalid_error,
											Toast.LENGTH_LONG).show();

								} else {
									Toast.makeText(getApplicationContext(),
											R.string.logn_generic_error,
											Toast.LENGTH_LONG).show();
								}
							}
						}
					});
			request.executeAsync();

		}
	}

	private void finishActivity() {
		// Start an intent for the dispatch activity
		Intent intent = new Intent(FacebookLogin.this, MainActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
		// | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void onCreateParse() {
		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
		ParseFacebookUtils.initialize("991607557523053");
	}

}
