package com.example.communityanimator;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.communityanimator.util.Application;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUp extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static int RESULT_LOAD_IMAGE = 1;
	private String selectedImagePath;
	// ADDED
	private String filemanagerstring;

	CategoriesAdapter dataAdapter = null;
	ParseUser user;
	List<ParseObject> ob;
	ProgressDialog mProgressDialog;
	ListView listView;
	EditText usernameEditText, passwordEditText, dateEditText,
			occupationEditText, emailEditText;
	RadioButton male, female;
	ImageView profileImage;
	byte[] image;
	LocationClient mLocationClient;
	Location location;
	String view;

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
		male = (RadioButton) findViewById(R.id.MaleRB);
		female = (RadioButton) findViewById(R.id.FemaleRB);
		profileImage = (ImageView) findViewById(R.id.ProfPic);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		Intent i = getIntent();
		if (i.hasExtra("MainView")) {
			view = "MainView";
		}

		Button uploadImage = (Button) findViewById(R.id.UploadImage);
		uploadImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open Gallery
				Log.d("SignUP", "open gallery");
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

		// Generate list View from ArrayList
		new RemoteDataTask().execute();
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

		Log.d("SignUP", "onActivityResult");
		if (resultCode == RESULT_OK) {
			if (requestCode == RESULT_LOAD_IMAGE) {

				Uri selectedImage = data.getData();

				// OI FILE Manager
				filemanagerstring = selectedImage.getPath();
				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImage);

				Bitmap bmp = null;
				try {
					bmp = decodeUri(selectedImage);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				profileImage.setImageBitmap(bmp);

				// Convert it to byte
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// Compress image to lower quality scale 1 - 100
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				image = stream.toByteArray();
			}
		}
	}

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 140;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

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
		for (int x = 0; x < listView.getChildCount(); x++) {
			cb = (CheckBox) listView.getChildAt(x).findViewById(R.id.checkBox1);

			if (cb.isChecked()) {
				interests.add(ob.get(x).getObjectId());
			}
		}
		// Add interest list in current user
		user.put("interestList", interests);

		// Verify if the user uploaded an image
		if (image.length != 0) {
			Log.d(Application.APPTAG, "entrou image not null!!");
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

		if (gender.equalsIgnoreCase("male")) {
			male.setChecked(true);
		} else {
			female.setChecked(true);
		}

		CheckBox cb;
		@SuppressWarnings("unchecked")
		ArrayList<String> userInterest = (ArrayList<String>) user
				.get("interestList");

		for (int i = 0; i < userInterest.size(); i++) {
			for (int j = 0; j < listView.getCount(); j++) {

				if (userInterest.get(i).equals(ob.get(j).getObjectId())) {
					cb = (CheckBox) listView.getChildAt(j).findViewById(
							R.id.checkBox1);
					cb.setChecked(true);
				}
			}
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

	// RemoteDataTask AsyncTask
	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Create a progressdialog
			mProgressDialog = new ProgressDialog(SignUp.this);
			// Set progressdialog message
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(false);
			// Show progressdialog
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Locate the class table named "Interest" in Parse.com
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
					"Interest");
			query.orderByAscending("_created_at");
			try {
				ob = query.find();
			} catch (ParseException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			ArrayList<Categories> categoriesList = new ArrayList<Categories>();

			// Retrieve object "interestName" from Parse.com database
			for (ParseObject categories : ob) {
				Categories c = new Categories(
						(String) categories.get("interestName"), false);
				categoriesList.add(c);
			}

			dataAdapter = new CategoriesAdapter(SignUp.this,
					R.layout.interest_items, categoriesList);

			// Binds the Adapter to the ListView
			listView.setAdapter(dataAdapter);

			listView.post(new Runnable() {
				@Override
				public void run() {

					if (view == "MainView") {
						profileView();
					}

				}
			});
			// Close the progressdialog
			mProgressDialog.dismiss();
		}
	}

}
