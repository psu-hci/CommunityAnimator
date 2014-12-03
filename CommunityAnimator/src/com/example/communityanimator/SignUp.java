package com.example.communityanimator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.communityanimator.scenarios.InterestExpandableListActivity;
import com.example.communityanimator.scenarios.TaskExpandableListActivity;
import com.example.communityanimator.util.Application;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUp extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static int RESULT_LOAD_IMAGE = 1;
	private String selectedImagePath;

	CategoriesAdapter dataAdapter = null;
	ParseUser user;
	EditText usernameEditText, passwordEditText, dateEditText,
			occupationEditText, emailEditText;
	ImageView profileImage;
	byte[] image;
	LocationClient mLocationClient;
	Location location;
	String view;
	TextView task, interest;

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
		task = (TextView) findViewById(R.id.taskList);
		interest = (TextView) findViewById(R.id.interestList);
		profileImage = (ImageView) findViewById(R.id.ProfPic);

		Intent i = getIntent();
		if (i.hasExtra("MainView")) {
			view = "MainView";
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
				signup();
			}
		});

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
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
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
		// TODO: verify duplicate username
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

		// if (male.isChecked()) {
		// gender = "male";
		// } else {
		// gender = "female";
		// }

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

		// save user location
		if (location != null) {
			ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
					location.getLongitude());
			user.put("location", point);
		} else {
			displayCurrentLocation();
		}

		// Get user interests
		CheckBox cb;
		ArrayList<String> interests = new ArrayList<String>();
		// TODO:verify this listview.getchildat and listview.getchildcount
		// for (int x = 0; x < listView.getChildCount(); x++) {
		// cb = (CheckBox) listView.getChildAt(x).findViewById(R.id.checkBox1);
		//
		// if (cb.isChecked()) {
		// interests.add(ob.get(x).getObjectId());
		// }
		// }
		// Add interest list in current user
		user.put("interestList", interests);

		// Verify if the user uploaded an image
		if (image.length != 0) {
			Log.d(Application.APPTAG, "entrou image not null!!");
			user.put("image", true);
			// Create the ParseFile to upload image
			ParseFile file = new ParseFile(user.getUsername() + ".png", image);
			// Upload the image into Parse Cloud
			file.saveInBackground();
			// Create a New Class called "ImageUpload" in Parse
			ParseObject imgupload = new ParseObject("imageUpload");
			// Create a column named "ImageName" and set the string
			imgupload.put("imageUser", username);
			// Create a column named "ImageName" and set the string
			imgupload.put("imageName", file.getName());
			// Create a column named "ImageFile" and insert the image
			imgupload.put("imageFile", file);
			// Create the class and the columns
			imgupload.saveInBackground();
		} else {
			user.put("image", false);
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

		// if (gender.equalsIgnoreCase("male")) {
		// male.setChecked(true);
		// } else {
		// female.setChecked(true);
		// }

		CheckBox cb;
		// @SuppressWarnings("unchecked")
		// ArrayList<String> userInterest = (ArrayList<String>) user
		// .get("interestList");
		//
		// for (int i = 0; i < userInterest.size(); i++) {
		// for (int j = 0; j < listView.getCount(); j++) {
		//
		// if (userInterest.get(i).equals(ob.get(j).getObjectId())) {
		// cb = (CheckBox) listView.getChildAt(j).findViewById(
		// R.id.checkBox1);
		// cb.setChecked(true);
		// }
		// }
		// }
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
