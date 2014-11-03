package com.example.communityanimator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.communityanimator.util.Application;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

public class TwitterLogin extends Activity {

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

	private void onLoginButtonClicked() {
		ParseTwitterUtils.logIn(TwitterLogin.this, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException arg1) {
				if (user == null) {
					Log.d(Application.APPTAG,
							"Uh oh. The user cancelled the Twitter login.");
				} else if (user.isNew()) {
					Log.d(Application.APPTAG,
							"User signed up and logged in through Twitter!");
				} else {
					Log.d(Application.APPTAG, "User logged in through Twitter!");
				}
			}

		});
	}

	public void onCreateParse() {
		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
		ParseTwitterUtils.initialize("TZy9crz2RePmmlycnaR2uCcQa",
				"lOfCu7zqDr9KttBs31R0ScRdItB5T2XGLSjAQzTBfzvZGHCbJI");
	}

}
