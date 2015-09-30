package com.example.communityanimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.twitter.Twitter;

public class TwitterLogin extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initializing Parse SDK
		// onCreateParse();
		// Calling ParseAnalytics to see Analytics of our app
		// ParseAnalytics.trackAppOpened(getIntent());
		onLoginButtonClicked();
	}

	private void onLoginButtonClicked() {
		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");

		ParseTwitterUtils.initialize("TZy9crz2RePmmlycnaR2uCcQa",
				"lOfCu7zqDr9KttBs31R0ScRdItB5T2XGLSjAQzTBfzvZGHCbJI");
		ParseTwitterUtils.logIn(TwitterLogin.this, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException arg1) {
				if (user == null) {
					Toast.makeText(
							getApplicationContext(),
							"There is no Twitter app or user cancel the account.",
							Toast.LENGTH_LONG).show();
				} else if (user.isNew()) {
					Twitter twitterUser = ParseTwitterUtils.getTwitter();
					if (twitterUser != null
							&& twitterUser.getScreenName().length() > 0) {

						user.put("username", twitterUser.getScreenName());
						user.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e != null) {
									Toast.makeText(getApplicationContext(),
											R.string.logn_generic_error,
											Toast.LENGTH_LONG).show();
								}
								finishActivity();
							}
						});
					}
				} else {
					finishActivity();
				}
			}

		});
	}

	private void finishActivity() {
		// Start an intent for the dispatch activity
		Intent intent = new Intent(TwitterLogin.this, SignUp.class);
		intent.putExtra("TwitterUser", "TwitterUser");
		startActivity(intent);
	}

	public void onCreateParse() {
		// Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
		// "NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
		ParseTwitterUtils.initialize("TZy9crz2RePmmlycnaR2uCcQa",
				"lOfCu7zqDr9KttBs31R0ScRdItB5T2XGLSjAQzTBfzvZGHCbJI");
	}

}
