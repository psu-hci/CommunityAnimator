package com.example.communityanimator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.communityanimator.scenarios.InterestExpandableListActivity;
import com.example.communityanimator.scenarios.TaskExpandableListActivity;
import com.example.communityanimator.util.Application;
import com.example.communityanimator.util.ConfigHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class SignUp extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static int RESULT_LOAD_IMAGE = 1;
	private String selectedImagePath;

	ParseUser user;
	EditText usernameEditText, passwordEditText, dateEditText,
			occupationEditText, emailEditText, genderEditText;
	ImageView profileImage;
	byte[] image = null;
	LocationClient mLocationClient;
	Location location;
	TextView task, interest;
	ArrayList<String> interestList, taskList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);

		// Setup SignUp form
		usernameEditText = (EditText) findViewById(R.id.NameET);
		passwordEditText = (EditText) findViewById(R.id.PasswordET);
		dateEditText = (EditText) findViewById(R.id.DOBET);
		occupationEditText = (EditText) findViewById(R.id.OccupationET);
		emailEditText = (EditText) findViewById(R.id.EmailET);
		genderEditText = (EditText) findViewById(R.id.GenderET);
		task = (TextView) findViewById(R.id.taskList);
		interest = (TextView) findViewById(R.id.interestList);
		profileImage = (ImageView) findViewById(R.id.ProfPic);
		interestList = new ArrayList<String>();
		taskList = new ArrayList<String>();

		Intent i = getIntent();
		if (i.hasExtra("MainView") || i.hasExtra("FacebookUser")
				|| i.hasExtra("TwitterUser")) {
			profileView();
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			interestList = bundle.getStringArrayList("interests");
			taskList = bundle.getStringArrayList("tasks");
		}

		task.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(SignUp.this,
						TaskExpandableListActivity.class);
				startActivity(i);
			}
		});

		interest.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(SignUp.this,
						InterestExpandableListActivity.class);
				startActivity(i);
			}
		});

		Button uploadImage = (Button) findViewById(R.id.UploadImage);
		uploadImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open Gallery
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select File"),
						RESULT_LOAD_IMAGE);
			}
		});

		Button saveProfile = (Button) findViewById(R.id.btn_Save);
		saveProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayCurrentLocation();
				attempSignup();
				// Clear previous Preference
				ConfigHelper.clearPreferences(getApplicationContext());
			}
		});

		// Create the LocationRequest object
		mLocationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String name = preferences.getString("Name", "");
		if (!name.equalsIgnoreCase("")) {
			usernameEditText.setText(name);
		}
		String password = preferences.getString("Password", "");
		if (!password.equalsIgnoreCase("")) {
			passwordEditText.setText(password);
		}
		String date = preferences.getString("Date", "");
		if (!date.equalsIgnoreCase("")) {
			dateEditText.setText(date);
		}
		String occupation = preferences.getString("Occupation", "");
		if (!occupation.equalsIgnoreCase("")) {
			occupationEditText.setText(occupation);
		}
		String email = preferences.getString("Email", "");
		if (!email.equalsIgnoreCase("")) {
			emailEditText.setText(email);
		}
		String gender = preferences.getString("Gender", "");
		if (!gender.equalsIgnoreCase("")) {
			genderEditText.setText(gender);
		}

		String interest = preferences.getString("Interest", "");
		if (!interest.equalsIgnoreCase("")) {
			List<String> myList = new ArrayList<String>();
			Collections.addAll(myList, interest.split("\\s*,\\s*"));
			interestList = (ArrayList<String>) myList;
		}

		String task = preferences.getString("Task", "");
		if (!task.equalsIgnoreCase("")) {
			List<String> myList = new ArrayList<String>();
			Collections.addAll(myList, task.split("\\s*,\\s*"));
			taskList = (ArrayList<String>) myList;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Store values between instances here
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		String strName = usernameEditText.getText().toString();
		String strPassword = passwordEditText.getText().toString();
		String strDate = dateEditText.getText().toString();
		String strOccupation = occupationEditText.getText().toString();
		String strEmail = emailEditText.getText().toString();
		String strGender = genderEditText.getText().toString();

		if (interestList != null) {
			String strInterest = interestList.toString();
			strInterest = strInterest.replace("[", "").replace("]", "");
			strInterest.trim();
			editor.putString("Interest", strInterest);
		}

		if (taskList != null) {
			String strTask = taskList.toString();
			strTask = strTask.replace("[", "").replace("]", "");
			strTask.trim();
			editor.putString("Task", strTask);
		}

		editor.putString("Name", strName);
		editor.putString("Password", strPassword);
		editor.putString("Date", strDate);
		editor.putString("Occupation", strOccupation);
		editor.putString("Email", strEmail);
		editor.putString("Gender", strGender);
		// Commit to storage
		editor.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == RESULT_LOAD_IMAGE) {

				Uri selectedImage = data.getData();

				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImage);
				Bitmap bm;
				BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
				bm = BitmapFactory.decodeFile(selectedImagePath, btmapOptions);
				profileImage.setImageBitmap(bm);

				// Convert it to byte
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// Compress image to lower quality scale 1 - 100
				bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
				image = stream.toByteArray();
				Log.d(Application.APPTAG, "image: " + image.length);
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	private void clearErrors() {
		usernameEditText.setError(null);
		passwordEditText.setError(null);
		dateEditText.setError(null);
		occupationEditText.setError(null);
		emailEditText.setError(null);
		genderEditText.setError(null);
	}

	private void attempSignup() {

		clearErrors();

		String username = usernameEditText.getText().toString().trim();
		String password = passwordEditText.getText().toString().trim();
		String date = dateEditText.getText().toString().trim();
		String occupation = occupationEditText.getText().toString().trim();
		String email = emailEditText.getText().toString().trim();
		String gender = genderEditText.getText().toString().trim();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(username)) {
			usernameEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = usernameEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(password)) {
			passwordEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = passwordEditText;
			cancel = true;
		} else if (password.length() < 4) {
			passwordEditText
					.setError(Html
							.fromHtml("<font color='red'>This password must be at least 5 characters long.</font>"));
			focusView = passwordEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(email)) {
			emailEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = emailEditText;
			cancel = true;
		} else if (!emailValidator(email)) {
			emailEditText
					.setError(Html
							.fromHtml("<font color='red'>This email address is invalid.</font>"));
			focusView = emailEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(date)) {
			dateEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = dateEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(occupation)) {
			occupationEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = occupationEditText;
			cancel = true;
		}
		if (TextUtils.isEmpty(gender)) {
			genderEditText
					.setError(Html
							.fromHtml("<font color='red'>This field is required.</font>"));
			focusView = genderEditText;
			cancel = true;
		}
		if (cancel) {
			focusView.requestFocus();
		} else {
			if (verifyFields()) {
				signUp(username.toLowerCase(Locale.getDefault()), password,
						email, date, occupation, gender);
			}
		}
	}

	private boolean verifyFields() {

		boolean validationError = false;
		StringBuilder validationErrorMessage = new StringBuilder(
				getString(R.string.logn_generic_error));

		if (interestList == null || interestList.isEmpty()) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_interest_required));
		}

		if (taskList == null || taskList.isEmpty()) {
			validationError = true;
			validationErrorMessage
					.append(getString(R.string.error_task_required));
		}

		if (validationError) {
			Toast.makeText(SignUp.this, validationErrorMessage.toString(),
					Toast.LENGTH_LONG).show();
			return false;
		} else
			return true;
	}

	private void signUp(String username, String password, String email,
			String date, String occupation, String gender) {
		// Set up a progress dialog
		final ProgressDialog dialog = new ProgressDialog(SignUp.this);
		dialog.setMessage(getString(R.string.progress_signUp));
		dialog.show();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			currentUser.setPassword(password);
			currentUser.put("dateBirth", date);
			currentUser.put("occupation", occupation);
			currentUser.put("gender", gender);
			currentUser.put("email", email);
			// default values
			currentUser.put("reminder", true);
			currentUser.put("status", false);
			currentUser.put("view", true);
			currentUser.put("distance", 0);
			currentUser.put("chatting", false);

			// save user location
			if (location != null) {
				ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
						location.getLongitude());
				currentUser.put("location", point);
			} else {
				displayCurrentLocation();
			}

			currentUser.put("interestList", interestList);
			currentUser.put("taskList", taskList);

			// Verify if the user uploaded an image
			Log.d(Application.APPTAG, "current User image: " + image.length);
			if (image == null || image.length < 0) {
				currentUser.put("image", false);
			} else {
				Log.d(Application.APPTAG, "entrou image not null!!");
				currentUser.put("image", true);
				ParseFile file = new ParseFile(user.getUsername() + ".png",
						image);
				file.saveInBackground();
				ParseObject imgupload = new ParseObject("imageUpload");
				imgupload.put("imageUser", username);
				imgupload.put("imageName", file.getName());
				imgupload.put("imageFile", file);
				imgupload.saveInBackground();
			}

			// Call the Parse update current user method
			currentUser.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
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
		} else {
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
			user.put("chatting", false);

			// save user location
			if (location != null) {
				ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
						location.getLongitude());
				user.put("location", point);
			} else {
				displayCurrentLocation();
			}

			user.put("interestList", interestList);
			user.put("taskList", taskList);

			// Verify if the user uploaded an image
			Log.d(Application.APPTAG, "new user image: " + image.length);
			if (image == null || image.length < 0) {
				user.put("image", false);
			} else {
				user.put("image", true);
				ParseFile file = new ParseFile(user.getUsername() + ".png",
						image);
				file.saveInBackground();
				ParseObject imgupload = new ParseObject("imageUpload");
				imgupload.put("imageUser", username);
				imgupload.put("imageName", file.getName());
				imgupload.put("imageFile", file);
				imgupload.saveInBackground();
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
	}

	// Show user profile
	private void profileView() {

		// Retrieve data from database
		ParseUser user = ParseUser.getCurrentUser();

		String username = user.getUsername();
		String password = user.getString("password");
		String date = user.getString("dateBirth");
		String occupation = user.getString("occupation");
		String email = user.getString("email");
		String gender = user.getString("gender");

		usernameEditText.setText(username);
		passwordEditText.setText(password);
		dateEditText.setText(date);
		occupationEditText.setText(occupation);
		emailEditText.setText(email);
		genderEditText.setText(gender);

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

	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

}
