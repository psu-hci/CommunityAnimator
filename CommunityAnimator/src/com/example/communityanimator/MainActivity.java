package com.example.communityanimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.communityanimator.database.User;
import com.example.communityanimator.message.MessagingActivity;
import com.example.communityanimator.util.Application;
import com.example.communityanimator.util.ErrorDialogFragment;
import com.example.communityanimator.util.LocationHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

public class MainActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// The update interval
	private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// A fast interval ceiling
	private static final int FAST_CEILING_IN_SECONDS = 1;
	// Update interval in milliseconds
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// A fast ceiling of update intervals, used when the app is visible
	private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* FAST_CEILING_IN_SECONDS;
	// Maximum results returned from a Parse query
	private static final int MAX_SEARCH_RESULTS = 20;
	// Maximum search radius for map in kilometers
	private static final int MAX_SEARCH_DISTANCE = 100;

	// Map fragment
	private GoogleMap mapFragment;
	LatLngBounds bounds;

	// Represents the circle around a map
	private Circle mapCircle;

	// Fields for the map radius in feet
	private float radius;
	private float lastRadius;

	// Fields for helping process map and location changes
	private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
	private int mostRecentMapUpdate;
	private boolean hasSetUpInitialLocation;
	private String selectedObjectId;
	private Location lastLocation;
	private Location currentLocation;
	ProgressDialog mProgressDialog;

	// A request to connect to Location Services
	private LocationRequest locationRequest;
	// Stores the current instantiation of the location client in this object
	private LocationClient locationClient;
	// Adapter for the Parse query
	private ParseQueryAdapter<ParseUser> userQueryAdapter;

	private ParseUser mUser = ParseUser.getCurrentUser();

	// ListView
	ListView list;
	CustomListAdapter adapter;
	List<ParseUser> contacts;
	List<ParseObject> listImages;
	private List<User> userData = null;
	// Define which view, distance and status the app will show
	boolean view, status;
	Integer distance;
	// Layouts
	RelativeLayout menuView, mapView;
	LinearLayout listView;
	TextView userStatus, userDistance;
	// Define available distance options
	private List<Float> availableOptions = Application.getConfigHelper()
			.getSearchDistanceAvailableOptions();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		radius = Application.getSearchDistance();
		lastRadius = radius;
		setContentView(R.layout.activity_main);

		// Create a new global location parameters object
		locationRequest = LocationRequest.create();

		// Set the update interval
		locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		locationRequest
				.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Create a new location client, using the enclosing class to handle
		// callbacks.
		locationClient = new LocationClient(this, this, this);

		// load user last definitions from menuoptions
		loadUserMenu();

		// load contacts as MapView
		initializeMap();

		final Button menu = (Button) findViewById(R.id.menu);
		menu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (menuView.getVisibility() == View.VISIBLE) {
					menuView.setVisibility(View.GONE);
					menu.setBackgroundResource(R.drawable.ic_menu_off);

				} else {
					menuView.setVisibility(View.VISIBLE);
					menu.setBackgroundResource(R.drawable.ic_menu_on);
				}

			}
		});

		TextView profileItem = (TextView) findViewById(R.id.profileItem);
		profileItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String flag = "MainView";
				Intent intent = new Intent(MainActivity.this, SignUp.class);
				intent.putExtra(flag, "MainView");
				startActivity(intent);

			}
		});

		TextView viewItem = (TextView) findViewById(R.id.viewItem);
		viewItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String[] MenuItems = { "List View", "Map View" };
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater inflater = getLayoutInflater();
				View convertView = inflater.inflate(R.layout.option_dialog,
						null);
				alertDialog.setView(convertView);
				alertDialog.setTitle("View");
				final ListView lv = (ListView) convertView
						.findViewById(R.id.listView1);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_list_item_single_choice,
						MenuItems);
				lv.setAdapter(adapter);

				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				// true = ListView(default)
				if (view) {
					lv.setItemChecked(0, true);
				} else {
					lv.setItemChecked(1, true);
				}

				// show it
				final AlertDialog dialog = alertDialog.create();
				dialog.show();

				Button btnSave = (Button) convertView
						.findViewById(R.id.listviewBtn);
				btnSave.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						Object checkedItem = lv.getAdapter().getItem(
								lv.getCheckedItemPosition());
						if (checkedItem.toString().equals("Map View")) {
							// Loading map
							view = false;
							initializeMap();
							listView.setVisibility(View.GONE);
							mapView.setVisibility(View.VISIBLE);
						} else {
							// Loading list
							view = true;
							listView.setVisibility(View.VISIBLE);
							mapView.setVisibility(View.GONE);
						}

						// Saving chosen view
						mUser.put("view", view);
						mUser.saveInBackground();
						dialog.dismiss();
					}
				});
			}
		});

		Button statusButton = (Button) findViewById(R.id.statusButton);
		statusButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				String[] StatusItems = { "animated", "busy" };
				AlertDialog.Builder alertDialogStatus = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater inflater = getLayoutInflater();
				View statusView = inflater
						.inflate(R.layout.option_dialog, null);

				alertDialogStatus.setView(statusView);
				alertDialogStatus.setTitle("Status");

				final ListView lv = (ListView) statusView
						.findViewById(R.id.listView1);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_list_item_single_choice,
						StatusItems);
				lv.setAdapter(adapter);

				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				// true = animated(default)
				if (status) {
					lv.setItemChecked(0, true);
				} else {
					lv.setItemChecked(1, true);
				}

				final AlertDialog dialog = alertDialogStatus.create();
				dialog.show();

				Button statusOk = (Button) statusView
						.findViewById(R.id.listviewBtn);
				statusOk.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						Object checkedItem = lv.getAdapter().getItem(
								lv.getCheckedItemPosition());
						userStatus.setText(checkedItem.toString());

						if (checkedItem.toString().equals("busy")) {
							status = false;
						} else {
							status = true;
						}
						// Save status on database
						mUser.put("status", status);
						mUser.saveInBackground();
						dialog.dismiss();
					}
				});

			}
		});

		Button distanceButton = (Button) findViewById(R.id.distanceButton);
		distanceButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String[] DistanceItems = { "50 feet away", "100 feet away",
						"250 feet away", "500 feet away" };

				AlertDialog.Builder alertDialogDistance = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater inflater = getLayoutInflater();
				View distanceView = inflater.inflate(R.layout.option_dialog,
						null);

				alertDialogDistance.setView(distanceView);
				alertDialogDistance.setTitle("Distance");

				final ListView lv = (ListView) distanceView
						.findViewById(R.id.listView1);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_list_item_single_choice,
						DistanceItems);
				lv.setAdapter(adapter);

				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

				switch (distance) {
				case 0:
					lv.setItemChecked(0, true);
					break;
				case 1:
					lv.setItemChecked(1, true);
					break;
				case 2:
					lv.setItemChecked(2, true);
					break;
				case 3:
					lv.setItemChecked(3, true);
					break;
				default:
					break;
				}

				final AlertDialog dialog = alertDialogDistance.create();
				dialog.show();

				Button distanceOk = (Button) distanceView
						.findViewById(R.id.listviewBtn);
				distanceOk.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						Object checkedItem = lv.getAdapter().getItem(
								lv.getCheckedItemPosition());
						userDistance.setText(checkedItem.toString());

						Application.setSearchDistance(availableOptions.get(lv
								.getCheckedItemPosition()));

						distance = lv.getCheckedItemPosition();
						// Save distance on database
						mUser.put("distance", lv.getCheckedItemPosition());
						mUser.saveInBackground();
						dialog.dismiss();
					}
				});
			}
		});

		TextView locateItem = (TextView) findViewById(R.id.locateItem);
		locateItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder alertDialogLocate = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater inflater = getLayoutInflater();
				View convertView = inflater.inflate(R.layout.locate_dialog,
						null);
				alertDialogLocate.setView(convertView);
				alertDialogLocate.setTitle("Locate");

				final EditText locate = (EditText) convertView
						.findViewById(R.id.et_locate);
				// show it
				final AlertDialog dialog = alertDialogLocate.create();
				dialog.show();

				Button locateOk = (Button) convertView
						.findViewById(R.id.locateOkBtn);
				locateOk.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Get name to locate
						String name = locate.getText().toString();
						if (name.matches("")) {
							Toast.makeText(MainActivity.this,
									"Please, enter your friend name.",
									Toast.LENGTH_SHORT).show();
							return;
						}
						// Locate user query
						Location myLoc = (currentLocation == null) ? lastLocation
								: currentLocation;
						final ParseGeoPoint myPoint = LocationHelper
								.geoPointFromLocation(myLoc);
						// Create the Parse query TODO:check query!
						ParseQuery<ParseUser> locateQuery = ParseUser
								.getQuery();
						locateQuery.whereEqualTo("username", name);
						locateQuery
								.findInBackground(new FindCallback<ParseUser>() {
									@Override
									public void done(List<ParseUser> objects,
											ParseException e) {
										if (e != null) {
											if (Application.APPDEBUG) {
												Log.d(Application.APPTAG,
														"An error occurred while querying.",
														e);
											}
											return;
										}
										if (objects.size() == 0) {
											Toast.makeText(MainActivity.this,
													"Contact not found.",
													Toast.LENGTH_LONG).show();
										} else {
											for (ParseUser user : objects) {
												MarkerOptions markerOpts = new MarkerOptions()
														.position(new LatLng(
																user.getParseGeoPoint(
																		"location")
																		.getLatitude(),
																user.getParseGeoPoint(
																		"location")
																		.getLongitude()));
												if (user.getParseGeoPoint(
														"location")
														.distanceInMilesTo(
																myPoint) > radius) {
													Toast.makeText(
															MainActivity.this,
															"The contact is out of range. Get closer!",
															Toast.LENGTH_LONG)
															.show();
												} else {
													markerOpts = markerOpts
															.title(user
																	.getUsername())
															.icon(BitmapDescriptorFactory
																	.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
												}
												// Add a new marker
												Marker marker = mapFragment
														.addMarker(markerOpts);
												mapMarkers.put(
														user.getObjectId(),
														marker);
												// Load Contacts
												contacts.add(user);
												adapter.notifyDataSetChanged();
											}
										}
									}
								});
						dialog.dismiss();
					}
				});
			}
		});

		TextView settingsItem = (TextView) findViewById(R.id.settingsItem);
		settingsItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this,
						PrefsActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStop() {
		// If the client is connected
		if (locationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		// After disconnect() is called, the client is considered "dead".
		locationClient.disconnect();

		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Connect to the location services client
		locationClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Application.getConfigHelper().fetchConfigIfNeeded();

		// Get the latest search distance preference
		radius = Application.getSearchDistance();
		// Checks the last saved location to show cached data if it's
		// available
		if (lastLocation != null) {
			LatLng myLatLng = new LatLng(lastLocation.getLatitude(),
					lastLocation.getLongitude());
			// If the search distance preference has been changed, move
			// map to new bounds.
			if (lastRadius != radius) {
				updateZoom(myLatLng);
			}
			// Update the circle map
			updateCircle(myLatLng);
		}
		// Save the current radius
		lastRadius = radius;
		// Query for the latest data to update the views.
		doMapQuery();
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		if (Application.APPDEBUG) {
			Log.d("Connected to location services", Application.APPTAG);
		}
		currentLocation = getLocation();
		startPeriodicUpdates();
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		if (Application.APPDEBUG) {
			Log.d("Disconnected from location services", Application.APPTAG);
		}
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {

				if (Application.APPDEBUG) {
					// Thrown if Google Play services canceled the original
					// PendingIntent
					Log.d(Application.APPTAG,
							"An error occurred when connecting to location services.",
							e);
				}
			}
		} else {
			// If no resolution is available, display a dialog to the user with
			// the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/*
	 * Report location updates to the UI.
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.d(Application.APPTAG, "onLocationChanged!");
		currentLocation = location;
		if (lastLocation != null
				&& LocationHelper.geoPointFromLocation(location)
						.distanceInMilesTo(
								LocationHelper
										.geoPointFromLocation(lastLocation)) < 0.007) {
			return;
		}
		lastLocation = location;
		LatLng myLatLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		if (!hasSetUpInitialLocation) {
			// Zoom to the current location.
			updateZoom(myLatLng);
			hasSetUpInitialLocation = true;
		}
		// Saving new user location
		saveUserLocation(location);

		// Update map radius indicator
		updateCircle(myLatLng);
		doMapQuery();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {
		// If the request code matches the code sent in onConnectionFailed
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				if (Application.APPDEBUG) {
					// Log the result
					Log.d(Application.APPTAG,
							"Connected to Google Play services");
				}
				break;
			// If any other result was returned by Google Play services
			default:
				if (Application.APPDEBUG) {
					// Log the result
					Log.d(Application.APPTAG,
							"Could not connect to Google Play services");
				}
				break;
			}
			// If any other request code was received
		default:
			if (Application.APPDEBUG) {
				// Report that this Activity received an unknown requestCode
				Log.d(Application.APPTAG,
						"Unknown request code received for the activity");
			}
			break;
		}
	}

	private void loadUserMenu() {
		// Check view chosen by user
		menuView = (RelativeLayout) findViewById(R.id.menuView);
		mapView = (RelativeLayout) findViewById(R.id.mapLayout);
		listView = (LinearLayout) findViewById(R.id.listLayout);
		userStatus = (TextView) findViewById(R.id.userStatus);
		userDistance = (TextView) findViewById(R.id.userDistance);

		view = mUser.getBoolean("view");
		if (view) {
			listView.setVisibility(View.VISIBLE);
			mapView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			mapView.setVisibility(View.VISIBLE);
		}

		// Check status chosen by user
		status = mUser.getBoolean("status");
		if (status) {
			userStatus.setText("animated");
		} else {
			userStatus.setText("busy");
		}

		// Check distance chosen by user
		float currentSearchDistance = Application.getSearchDistance();
		if (!availableOptions.contains(currentSearchDistance)) {
			availableOptions.add(currentSearchDistance);
		}
		Collections.sort(availableOptions);

		distance = mUser.getInt("distance");
		switch (distance) {
		case 0:
			userDistance.setText("50 feet away");
			break;
		case 1:
			userDistance.setText("100 feet away");
			break;
		case 2:
			userDistance.setText("250 feet away");
			break;
		case 3:
			userDistance.setText("500 feet away");
			break;
		default:
			break;
		}
	}

	private void initializeList() {
		Log.d(Application.APPTAG, "initializeList!");

		list = (ListView) findViewById(R.id.list);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		// Send a notification or chat
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Log.d(Application.APPTAG, "onItemCLick!");
				TextView name = (TextView) view.findViewById(R.id.contactName);
				verifyAddorChat(name.getText().toString());
			}
		});

		if (userData == null)
			getUserImages(contacts);

		adapter = new CustomListAdapter(MainActivity.this, userData);
		list.setVisibility(View.VISIBLE);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	private void initializeMap() {
		Log.d(Application.APPTAG, "initializeMap!");
		// Set up the map fragment
		mapFragment = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();
		// Enable the current location "blue dot"
		mapFragment.setMyLocationEnabled(true);
		mapFragment
				.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker marker) {
						Log.d(Application.APPTAG, "onMarkerCLick!");
						verifyAddorChat(marker.getTitle());
					}
				});

		// Set up the camera change handler
		mapFragment.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				// When the camera changes, update the query
				doMapQuery();
			}
		});
	}

	private void getUserImages(List<ParseUser> contacts) {
		// Create the array
		userData = new ArrayList<User>();

		// Locate the class table named "ImageUpload" in Parse.com
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"imageUpload");
		try {
			listImages = query.find();
			for (int i = 0; i < contacts.size(); i++) {
				if (contacts.get(i).getBoolean("image")) {

					for (ParseObject user : listImages) {
						if (contacts.get(i).getUsername()
								.equals(user.getString("imageUser"))) {
							ParseFile fileObject = user
									.getParseFile("imageFile");
							User us = new User();
							us.setStatus(contacts.get(i).getBoolean("status"));
							us.setPhotoFile(fileObject.getData());
							us.setUsername(contacts.get(i)
									.getString("username"));
							us.setChatting(contacts.get(i).getBoolean(
									"chatting"));
							userData.add(us);
						}
					}
				} else {
					User us = new User();
					us.setStatus(contacts.get(i).getBoolean("status"));
					us.setPhotoFile(null);
					us.setUsername(contacts.get(i).getString("username"));
					us.setChatting(contacts.get(i).getBoolean("chatting"));
					userData.add(us);
				}
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	}

	private void verifyAddorChat(String recipientName) {

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", recipientName);
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {

				if (e == null) {
					boolean chat = user.getBoolean("chatting");
					if (chat) {
						callMessaging(mUser.getObjectId(), user.getObjectId());
					} else {
						pushNotification(user.getUsername());
					}
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}
			}
		});
	}

	private void callMessaging(String currentUserId, final String recipientId) {

		ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
		query.whereEqualTo("senderId", currentUserId);
		query.whereEqualTo("recipientId", recipientId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject object, ParseException e) {

				if (e == null) {
					Intent intent = new Intent(getApplicationContext(),
							MessagingActivity.class);
					intent.putExtra("RECIPIENT_ID", recipientId);
					startActivity(intent);
				}
			}
		});
	}

	private void pushNotification(String user) {
		Log.d(Application.APPTAG, "pushNotification!");
		// Find receiver status by name
		ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
		userQuery.whereEqualTo("username", user);
		userQuery.getFirstInBackground(new GetCallback<ParseUser>() {

			@SuppressWarnings("static-access")
			@Override
			public void done(ParseUser user, ParseException e) {

				if (e == null) {
					boolean status = user.getBoolean("status");
					if (status) {

						JSONObject obj;

						try {
							obj = new JSONObject();
							obj.put("alert",
									"Hi! You receive a friend request.");
							obj.put("action",
									"com.example.communityanimator.UPDATE_STATUS");
							obj.put("customdata", mUser.getUsername() + "/"
									+ user.getUsername());

							ParsePush push = new ParsePush();
							ParseQuery<ParseInstallation> query = ParseInstallation
									.getQuery();

							// Notification for Android users
							query.whereEqualTo("user", user.getUsername());
							push.setQuery(query);
							push.setData(obj);
							push.sendInBackground();

							Toast.makeText(getApplicationContext(),
									"Friend Request sent!", Toast.LENGTH_LONG)
									.show();
						} catch (JSONException ex) {
							ex.printStackTrace();
						}

					} else {
						Toast.makeText(getApplicationContext(),
								"This contact is not animated to chat!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}
			}
		});
	}

	/*
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {
		Log.d(Application.APPTAG, "serviceConnected!");
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			if (Application.APPDEBUG) {
				// In debug mode, log the status
				Log.d(Application.APPTAG, "Google play services available");
			}
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getFragmentManager(), Application.APPTAG);
			}
			return false;
		}
	}

	/*
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	/*
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		locationClient.removeLocationUpdates(this);
	}

	/*
	 * Get the current location
	 */
	private Location getLocation() {
		Log.d(Application.APPTAG, "getLocation!");
		// If Google Play Services is available
		if (servicesConnected()) {
			// Get the current location
			return locationClient.getLastLocation();
		} else {
			return null;
		}
	}

	/*
	 * Save the user location on database
	 */
	private void saveUserLocation(Location location) {
		// TODO: verify saving user location
		ParseGeoPoint newPoint = LocationHelper.geoPointFromLocation(location);
		mUser.put("location", newPoint);
		mUser.saveInBackground();
	}

	/*
	 * Set up the query to update the map view
	 */
	private void doMapQuery() {
		Log.d(Application.APPTAG, "doMapQuery!");

		final int myUpdateNumber = ++mostRecentMapUpdate;
		Location myLoc = (currentLocation == null) ? lastLocation
				: currentLocation;
		// If location info isn't available, clean up any existing markers
		if (myLoc == null) {
			LocationHelper.cleanUpMarkers(new HashSet<String>());
			return;
		}

		final ParseGeoPoint myPoint = LocationHelper
				.geoPointFromLocation(myLoc);
		// Create the map Parse query
		ParseQuery<ParseUser> mapQuery = ParseUser.getQuery();
		// Set up additional query filters
		mapQuery.whereWithinMiles("location", myPoint, MAX_SEARCH_DISTANCE);
		mapQuery.whereNotEqualTo("username", mUser.getUsername());
		mapQuery.whereContainedIn("interestList", mUser.getList("interestList"));
		mapQuery.orderByAscending("username");
		mapQuery.setLimit(MAX_SEARCH_RESULTS);
		mapQuery.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				if (e != null) {
					if (Application.APPDEBUG) {
						Log.d(Application.APPTAG,
								"An error occurred while querying.", e);
					}
					return;
				}
				/*
				 * Make sure we're processing results from the most recent
				 * update, in case there may be more than one in progress.
				 */
				if (myUpdateNumber != mostRecentMapUpdate) {
					return;
				}
				// Load contacts arraylist
				contacts = objects;
				// Initialize contacts list
				initializeList();
				// Contacts to show on the map
				Set<String> toKeep = new HashSet<String>();
				// Loop through the results of the search
				for (ParseUser user : objects) {
					// Add this contact to the list of map pins to keep
					toKeep.add(user.getObjectId());
					// Check for an existing marker for this contact
					Marker oldMarker = mapMarkers.get(user.getObjectId());
					// Set up the map marker's location
					MarkerOptions markerOpts = new MarkerOptions()
							.position(new LatLng(user.getParseGeoPoint(
									"location").getLatitude(), user
									.getParseGeoPoint("location")
									.getLongitude()));
					// Set up the marker properties based on if it is within the
					// search radius
					if (user.getParseGeoPoint("location").distanceInMilesTo(
							myPoint) > radius) {
						// Check for an existing out of range marker
						if (oldMarker != null) {
							if (oldMarker.getSnippet() == null) {
								// Out of range marker already exists, skip
								// adding it
								continue;
							} else {
								// Marker now out of range, needs to be
								// refreshed
								oldMarker.remove();
							}
						}
						markerOpts = markerOpts
								.title(getResources().getString(
										R.string.post_out_of_range))
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					} else {
						// Check for an existing in range marker
						if (oldMarker != null) {
							if (oldMarker.getSnippet() != null) {
								// In range marker already exists, skip adding
								// it
								continue;
							} else {
								// Marker now in range, needs to be refreshed
								oldMarker.remove();
							}
						}
						// Display a green marker with the contact information
						String status;
						if (user.getBoolean("status"))
							status = "animated";
						else
							status = "busy";
						markerOpts = markerOpts
								.title(user.getUsername())
								.snippet(status)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					}
					// Add a new marker
					Marker marker = mapFragment.addMarker(markerOpts);
					mapMarkers.put(user.getObjectId(), marker);
					if (user.getObjectId().equals(selectedObjectId)) {
						marker.showInfoWindow();
						selectedObjectId = null;
					}
				}
				// Clean up old markers.
				LocationHelper.cleanUpMarkers(toKeep);
			}
		});
	}

	/*
	 * Displays a circle on the map representing the search radius
	 */
	private void updateCircle(LatLng myLatLng) {
		Log.d(Application.APPTAG, "updateCircle!");
		if (mapCircle == null) {
			mapCircle = mapFragment.addCircle(new CircleOptions().center(
					myLatLng).radius(radius));
			int baseColor = Color.DKGRAY;
			mapCircle.setStrokeColor(baseColor);
			mapCircle.setStrokeWidth(2);
			mapCircle.setFillColor(Color.argb(50, Color.red(baseColor),
					Color.green(baseColor), Color.blue(baseColor)));
		}
		mapCircle.setCenter(myLatLng);
		mapCircle.setRadius(radius); // Convert radius in feet
	}

	/*
	 * Zooms the map to show the area of interest based on the search radius
	 */
	private void updateZoom(LatLng myLatLng) {
		Log.d(Application.APPTAG, "updateZoom!");
		// Get the bounds to zoom
		bounds = LocationHelper.calculateBoundsWithCenter(myLatLng);
		mapFragment.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				// When the camera changes, update the query
				mapFragment.animateCamera(CameraUpdateFactory.newLatLngBounds(
						bounds, 10));
			}
		});
	}

	/*
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 */
	private void showErrorDialog(int errorCode) {
		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
		// If Google Play services can provide an error dialog
		if (errorDialog != null) {
			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);
			// Show the error dialog in the DialogFragment
			errorFragment.show(getFragmentManager(), Application.APPTAG);
		}
	}

}
