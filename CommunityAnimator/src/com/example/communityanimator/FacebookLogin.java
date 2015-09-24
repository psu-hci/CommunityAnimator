package com.example.communityanimator;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.communityanimator.util.Application;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FacebookLogin extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// GET FACEBOOK KEY HASH
		// try {
		// PackageInfo info = getPackageManager().getPackageInfo(
		// "com.example.communityanimator",
		// PackageManager.GET_SIGNATURES);
		// for (Signature signature : info.signatures) {
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(signature.toByteArray());
		// Log.d("KeyHash:",
		// Base64.encodeToString(md.digest(), Base64.DEFAULT));
		// }
		// } catch (NameNotFoundException e) {
		//
		// } catch (NoSuchAlgorithmException e) {
		//
		// }

		// Initializing Parse SDK
		onCreateParse();
		// Calling ParseAnalytics to see Analytics of our app
		// ParseAnalytics.trackAppOpened(getIntent());
		onLoginButtonClicked();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
	}

	private void onLoginButtonClicked() {

		final List<String> permissions = Arrays.asList("public_profile",
				"email");
		ParseFacebookUtils.logInWithReadPermissionsInBackground(
				FacebookLogin.this, permissions, new LogInCallback() {
					@Override
					public void done(final ParseUser user, ParseException err) {
						if (user != null) {
							if (user.isNew()) {
								// makeMeRequest();
								Log.d(Application.APPTAG,
										"User signed up and logged in through Facebook!");

								if (!ParseFacebookUtils.isLinked(user)) {

									ParseFacebookUtils
											.linkWithReadPermissionsInBackground(
													user, FacebookLogin.this,
													permissions,
													new SaveCallback() {
														@Override
														public void done(
																ParseException ex) {
															if (ParseFacebookUtils
																	.isLinked(user)) {
																Log.d("MyApp",
																		"Woohoo, user logged in with Facebook!");
															}
														}
													});
								} else {
									Toast.makeText(
											getApplicationContext(),
											"You can change your personal data in Settings tab!",
											Toast.LENGTH_SHORT).show();
								}
							} else {

								Log.d("MyApp",
										"User logged in through Facebook!");

								if (!ParseFacebookUtils.isLinked(user)) {
									ParseFacebookUtils
											.linkWithReadPermissionsInBackground(
													user, FacebookLogin.this,
													permissions,
													new SaveCallback() {
														@Override
														public void done(
																ParseException ex) {
															if (ParseFacebookUtils
																	.isLinked(user)) {
																Log.d("MyApp",
																		"Woohoo, user logged in with Facebook!");

																finishActivity();
															}
														}
													});
								}

							}
						} else {
							Toast.makeText(
									getApplicationContext(),
									"There is no Facebook app or user cancel the account.",
									Toast.LENGTH_LONG).show();
						}
					}

				});
	}

	// private void makeMeRequest() {
	// Log.d(Application.APPTAG, "makeMeRequest");
	// Session session = ParseFacebookUtils.getSession();
	// if (session != null && session.isOpened()) {
	// Request request = Request.newMeRequest(
	// ParseFacebookUtils.getSession(),
	// new Request.GraphUserCallback() {
	// @Override
	// public void onCompleted(GraphUser user,
	// Response response) {
	// ParseUser parseUser = ParseUser.getCurrentUser();
	// if (user != null && parseUser != null
	// && user.getName().length() > 0) {
	// Log.d(Application.APPTAG, "facebook user: "
	// + user.getFirstName());
	// parseUser.put("username", user.getFirstName()
	// .toLowerCase(Locale.getDefault()));
	// parseUser.saveInBackground(new SaveCallback() {
	//
	// @Override
	// public void done(ParseException arg0) {
	// finishActivity();
	// }
	// });
	//
	// } else if (response.getError() != null) {
	// if ((response.getError().getCategory() ==
	// FacebookRequestError.Category.AUTHENTICATION_RETRY)
	// || (response.getError().getCategory() ==
	// FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
	// Toast.makeText(getApplicationContext(),
	// R.string.session_invalid_error,
	// Toast.LENGTH_LONG).show();
	//
	// } else {
	// Toast.makeText(getApplicationContext(),
	// R.string.logn_generic_error,
	// Toast.LENGTH_LONG).show();
	// }
	// }
	// }
	// });
	// request.executeAsync();
	//
	// }
	// }

	private void finishActivity() {
		// Start an intent for the dispatch activity
		Intent intent = new Intent(FacebookLogin.this, SignUp.class);
		intent.putExtra("FacebookUser", "FacebookUser");
		startActivity(intent);
	}

	public void onCreateParse() {
		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
		ParseFacebookUtils.initialize(getApplicationContext());
	}

}
