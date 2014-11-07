package com.example.communityanimator;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUp extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	CategoriesAdapter dataAdapter = null;
	ParseUser user;
	String view;
	EditText usernameEditText, passwordEditText, dateEditText,
			occupationEditText, emailEditText;
	RadioButton male, female;
	LocationClient mLocationClient;
	Location location;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);

		Intent i = getIntent();

		if (i.hasExtra("MainView")) {
			view = i.getStringExtra("MainView");
			profileView();
		}

		// Setup SignUp form
		usernameEditText = (EditText) findViewById(R.id.NameET);
		passwordEditText = (EditText) findViewById(R.id.PasswordET);
		dateEditText = (EditText) findViewById(R.id.DOBET);
		occupationEditText = (EditText) findViewById(R.id.OccupationET);
		emailEditText = (EditText) findViewById(R.id.EmailET);
		male = (RadioButton) findViewById(R.id.MaleRB);
		female = (RadioButton) findViewById(R.id.FemaleRB);

		Button saveProfile = (Button) findViewById(R.id.btn_Save);
		saveProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayCurrentLocation();
				signup();
			}
		});

		// Generate list View from ArrayList
		displayListView();

		// Create the LocationRequest object
		mLocationClient = new LocationClient(this, this, this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	private void displayListView() {

		// Array list of countries
		ArrayList<Categories> countryList = new ArrayList<Categories>();
		Categories country = new Categories("Agriculture and Food", false);
		countryList.add(country);
		country = new Categories("Arts and Culture", true);
		countryList.add(country);
		country = new Categories("Built Environment", false);
		countryList.add(country);
		country = new Categories("Business and Entrepreneurship", true);
		countryList.add(country);
		country = new Categories("Civic Engagement", true);
		countryList.add(country);
		country = new Categories("Communication", false);
		countryList.add(country);
		country = new Categories("Community", false);
		countryList.add(country);
		country = new Categories("Economy", false);
		countryList.add(country);
		country = new Categories("Education and Learning", false);
		countryList.add(country);
		country = new Categories("Energy", false);
		countryList.add(country);
		country = new Categories("Environment and Sustainability", false);
		countryList.add(country);
		country = new Categories("Health and Wellness", false);
		countryList.add(country);
		country = new Categories("Justice and Equality", false);
		countryList.add(country);
		country = new Categories("Technology", false);
		countryList.add(country);
		country = new Categories("Transportation", false);
		countryList.add(country);

		// create an ArrayAdaptar from the String Array
		dataAdapter = new CategoriesAdapter(this, R.layout.interest_items,
				countryList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
	}

	private void signup() {

		String username = usernameEditText.getText().toString().trim();
		String password = passwordEditText.getText().toString().trim();
		String date = dateEditText.getText().toString().trim();
		String occupation = occupationEditText.getText().toString().trim();
		String email = emailEditText.getText().toString().trim();
		String gender = null;

		// Validate the sign up data
		boolean validationError = false;
		StringBuilder validationErrorMessage = new StringBuilder(
				getString(R.string.logn_generic_error));
		if (username.length() == 0) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_field_required));
		}
		if (password.length() == 0) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_field_required));
		}
		if (password.length() < 5) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_invalid_password));
		}

		if (date.length() == 0) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_field_required));
		}

		if (occupation.length() == 0) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_field_required));
		}

		if (email.length() == 0) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_field_required));
		}
		if (!emailValidator(email)) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_invalid_email));
		}

		if (male.isChecked()) {
			gender = "male";
		} else {
			gender = "female";
		}

		// If there is a validation error, display the error
		if (validationError) {
			Toast.makeText(SignUp.this, validationErrorMessage.toString(),
					Toast.LENGTH_LONG).show();
			return;
		}

		// Set up a progress dialog
		final ProgressDialog dialog = new ProgressDialog(SignUp.this);
		dialog.setMessage(getString(R.string.progress_signUp));
		dialog.show();

		// Set up a new Parse user
		user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		user.put("dateBirth", date);
		user.put("occupation", occupation);
		user.put("gender", gender);
		user.put("email", email);
		// default values
		user.put("reminder", true);
		user.put("status", false);
		user.put("view", true);
		user.put("distance", 0);

		if (location != null) {
			ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
					location.getLongitude());
			user.put("location", point);
		} else {
			displayCurrentLocation();
		}

		// Call the Parse signup method
		user.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				dialog.dismiss();
				if (e != null) {
					// Show the error message
					Toast.makeText(SignUp.this, e.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					// Start an intent for the dispatch activity
					Intent intent = new Intent(SignUp.this,
							DispatchActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
		});
	}

	// Show user profile
	private void profileView() {

		// Retrieve data from database
		ParseUser user = ParseUser.getCurrentUser();
		String username = user.getUsername();
		String date = user.getString("dateBirth");
		String occupation = user.getString("occupation");
		String email = user.getString("email");
		String gender = user.getString("gender");

		usernameEditText.setText(username);
		dateEditText.setText(date);
		occupationEditText.setText(occupation);
		emailEditText.setText(email);

		if (gender.equalsIgnoreCase("male")) {
			male.setChecked(true);
		} else {
			female.setChecked(true);
		}

	}

	/**
	 * validate your email address format. Ex-akhi@mani.com
	 */
	public boolean emailValidator(String email) {
		Pattern pattern;
		Matcher matcher;
		final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public void displayCurrentLocation() {
		// Get the current location's latitude & longitude
		Location currentLocation = mLocationClient.getLastLocation();
		location = currentLocation;
		String msg = "Current Location: "
				+ Double.toString(currentLocation.getLatitude()) + ","
				+ Double.toString(currentLocation.getLongitude());

		Log.d("SignUp", "location: " + msg);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Display the error code on failure
		Toast.makeText(this,
				"Connection Failure : " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// Display the connection status
		Log.d("SignUp", "Connected!");
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

}
