package com.example.communityanimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.communityanimator.util.Application;
import com.facebook.AccessToken;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class FacebookLogin extends Activity {

	ParseUser user;
	String name = null, email = null;

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
		ParseUser currentUser = ParseUser.getCurrentUser();

		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			Log.d(Application.APPTAG, "calll next activity");
			finishActivity();
		} else {
			Log.d(Application.APPTAG, "log in");
			onLoginButtonClicked();
		}

	}

	public boolean isLoggedIn() {
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		return accessToken != null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
	}

	private void onLoginButtonClicked() {
		Log.d(Application.APPTAG, "onLoginButtonClicked");

		// Toast.makeText(FacebookLogin.this,"Wait...",Toast.LENGTH_SHORT).show();
		// GraphRequest request = GraphRequest.newMeRequest(
		// AccessToken.getCurrentAccessToken(),
		// new GraphRequest.GraphJSONObjectCallback() {
		// @Override
		// public void onCompleted(JSONObject object,
		// GraphResponse response) {
		// Log.d(Application.APPTAG, "onCompleted");
		// try {
		// email_id = object.getString("email");
		// gender = object.getString("gender");
		// // Start new activity or use this
		// // info in your project.
		// Intent i = new Intent(FacebookLogin.this,
		// SignUp.class);
		// i.putExtra("FacebookUser", "FacebookUser");
		// i.putExtra("type", "facebook");
		// i.putExtra("facebook_id",
		// facebook_id);
		// i.putExtra("f_name", f_name);
		// i.putExtra("m_name", m_name);
		// i.putExtra("l_name", l_name);
		// i.putExtra("full_name",
		// full_name);
		// i.putExtra("profile_image",
		// profile_image);
		// i.putExtra("email_id", email_id);
		// i.putExtra("gender", gender);

		// startActivity(i);
		// finish();
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// // e.printStackTrace();
		// }
		//
		// }
		//
		// });
		//
		// request.executeAsync();

		// ParseFacebookUtils.logInWithReadPermissionsInBackground(
		// FacebookLogin.this, mPermissions, new LogInCallback() {
		// @Override
		// public void done(ParseUser user, ParseException err) {
		// Log.d(Application.APPTAG, "user: " + user.getUsername());
		//
		// if (user.isNew()) {
		// Log.d(Application.APPTAG,
		// "User signed up and logged in through Facebook!");
		// getUserDetailsFromFB(user);
		// // saveNewUser();
		// } else {
		// Log.d(Application.APPTAG,
		// "User logged in through Facebook!");
		// finishActivity();
		// }
		// }
		// });
	}

	// private void getUserDetailsFromFB(final ParseUser user) {
	// GraphRequestAsyncTask request = GraphRequest.newMeRequest(
	// AccessToken.getCurrentAccessToken(),
	// new GraphRequest.GraphJSONObjectCallback() {
	// @Override
	// public void onCompleted(JSONObject user,
	// GraphResponse response) {
	// /* handle the result */
	// try {
	// email = response.getJSONObject().getString("email");
	// name = response.getJSONObject().getString("name");
	//
	// saveNewUser();
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	// }).executeAsync();
	// }

	// private void saveNewUser() {
	//
	// ParseUser currentUser = ParseUser.getCurrentUser();
	// if (currentUser != null) {
	// currentUser.put("username", name);
	// currentUser.put("email", email);
	// currentUser.saveInBackground(new SaveCallback() {
	//
	// @Override
	// public void done(ParseException e) {
	// if (e != null) {
	// // Show the error message
	// Toast.makeText(FacebookLogin.this, e.getMessage(),
	// Toast.LENGTH_LONG).show();
	// } else {
	// finishActivity();
	// }
	// }
	// });
	// } else {
	// user = new ParseUser();
	// user.setUsername(name);
	// user.put("email", email);
	// user.saveInBackground(new SaveCallback() {
	//
	// @Override
	// public void done(ParseException e) {
	// if (e != null) {
	// // Show the error message
	// Toast.makeText(FacebookLogin.this, e.getMessage(),
	// Toast.LENGTH_LONG).show();
	// } else {
	// finishActivity();
	// }
	// }
	// });
	// }
	// }

	private void finishActivity() {
		// Start an intent for the dispatch activity
		Intent intent = new Intent(FacebookLogin.this, SignUp.class);
		intent.putExtra("FacebookUser", "FacebookUser");
		startActivity(intent);
	}

	public void onCreateParse() {
		// Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
		// "NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");
		ParseFacebookUtils.initialize(getApplicationContext());
	}

}
