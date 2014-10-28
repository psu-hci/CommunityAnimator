package com.example.communityanimator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.communityanimator.util.Application;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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

	/*
	 * Constants for location update parameters
	 */
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

	/*
	 * Constants for handling location results
	 */
	// Initial offset for calculating the map bounds
	private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

	// Accuracy for calculating the map bounds
	private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

	// Maximum results returned from a Parse query
	private static final int MAX_POST_SEARCH_RESULTS = 20;

	// Maximum post search radius for map in kilometers
	private static final int MAX_POST_SEARCH_DISTANCE = 100;

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
	private String selectedPostObjectId;
	private Location lastLocation;
	private Location currentLocation;

	// A request to connect to Location Services
	private LocationRequest locationRequest;

	// Stores the current instantiation of the location client in this object
	private LocationClient locationClient;

	// Adapter for the Parse query
	private ParseQueryAdapter<ParseUser> userQueryAdapter;

	// ListView
	private ListView list;
	// Define which view, distance and status the app will show
	Object view, status, distance;
	// Layouts
	RelativeLayout menuView, mapView;
	LinearLayout listView;
	TextView userStatus, userDistance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(Application.APPTAG, "onCreate!");
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

		// Set up a customized query
		ParseQueryAdapter.QueryFactory<ParseUser> factory = new ParseQueryAdapter.QueryFactory<ParseUser>() {
			@Override
			public ParseQuery<ParseUser> create() {
				Location myLoc = (currentLocation == null) ? lastLocation
						: currentLocation;

				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.include("user");
				query.orderByDescending("createdAt");
				query.whereWithinMiles("location", geoPointFromLocation(myLoc),
						radius);
				query.setLimit(MAX_POST_SEARCH_RESULTS);
				return query;
			}
		};

		// Set up the query adapter
		userQueryAdapter = new ParseQueryAdapter<ParseUser>(this, factory) {
			@Override
			public View getItemView(ParseUser user, View view, ViewGroup parent) {
				if (view == null) {
					view = View.inflate(getContext(), R.layout.map_item, null);
				}
				TextView contentView = (TextView) view
						.findViewById(R.id.content_view);
				TextView usernameView = (TextView) view
						.findViewById(R.id.username_view);

				Object status = user.getBoolean("status");
				Log.d(Application.APPTAG, "status: " + status);
				if (status.equals(true)) {
					contentView.setText("animated");
				} else {
					contentView.setText("busy");
				}

				usernameView.setText(user.getUsername());
				return view;
			}
		};

		// Disable automatic loading when the adapter is attached to a view.
		userQueryAdapter.setAutoload(false);

		// Disable pagination, we'll manage the query limit ourselves
		userQueryAdapter.setPaginationEnabled(false);

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
				// TODO call Liam´s profile screen

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
				View convertView = inflater.inflate(R.layout.view_dialog, null);
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
				if (view.equals(true)) {
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
						ParseUser user = ParseUser.getCurrentUser();
						user.put("view", view);
						user.saveInBackground();

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
						.inflate(R.layout.status_dialog, null);

				alertDialogStatus.setView(statusView);
				alertDialogStatus.setTitle("Status");

				final Spinner userInput = (Spinner) statusView
						.findViewById(R.id.dialog_spinner);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_spinner_item, StatusItems);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				userInput.setAdapter(adapter);

				// show it
				final AlertDialog dialog = alertDialogStatus.create();
				dialog.show();

				Button statusOk = (Button) statusView
						.findViewById(R.id.statusOkBtn);
				statusOk.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Save status on database
						String Text = userInput.getSelectedItem().toString();
						userStatus.setText(Text);

						boolean flag = true;
						if (Text == "busy")
							flag = false;
						// Save status on database
						ParseUser user = ParseUser.getCurrentUser();
						user.put("status", flag);
						user.saveInBackground();

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
				View distanceView = inflater.inflate(R.layout.distance_dialog,
						null);

				alertDialogDistance.setView(distanceView);
				alertDialogDistance.setTitle("Distance");

				final Spinner userInput = (Spinner) distanceView
						.findViewById(R.id.distance_spinner);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_spinner_item, DistanceItems);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				userInput.setAdapter(adapter);

				// show it
				final AlertDialog dialog = alertDialogDistance.create();
				dialog.show();

				Button statusOk = (Button) distanceView
						.findViewById(R.id.distanceOkBtn);
				statusOk.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Save status on database
						String Text = userInput.getSelectedItem().toString();
						userDistance.setText(Text);

						// Save distance on database
						ParseUser user = ParseUser.getCurrentUser();
						user.put("distance",
								userInput.getSelectedItemPosition());
						user.saveInBackground();

						dialog.dismiss();

					}
				});
			}
		});

		TextView locateItem = (TextView) findViewById(R.id.locateItem);
		locateItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		TextView settingsItem = (TextView) findViewById(R.id.settingsItem);
		settingsItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void loadUserMenu() {
		// Check view chosen by user

		menuView = (RelativeLayout) findViewById(R.id.menuView);
		mapView = (RelativeLayout) findViewById(R.id.mapLayout);
		listView = (LinearLayout) findViewById(R.id.listLayout);
		userStatus = (TextView) findViewById(R.id.userStatus);
		userDistance = (TextView) findViewById(R.id.userDistance);

		view = ParseUser.getCurrentUser().get("view");
		if (view.equals(true)) {
			listView.setVisibility(View.VISIBLE);
			mapView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			mapView.setVisibility(View.VISIBLE);
		}

		// Check status chosen by user
		status = ParseUser.getCurrentUser().get("status");
		if (status.equals(true)) {
			userStatus.setText("animated");
		} else {
			userStatus.setText("busy");
		}

		// Check distance chosen by user
		distance = ParseUser.getCurrentUser().get("distance");
		switch ((Integer) distance) {
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

	@Override
	protected void onStop() {
		Log.d(Application.APPTAG, "onStop!");
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
		Log.d(Application.APPTAG, "onStart!");
		super.onStart();

		// Connect to the location services client
		locationClient.connect();
	}

	@Override
	protected void onResume() {
		Log.d(Application.APPTAG, "onResume!");
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
		doListQuery();
	}

	private void initializeList(List<ParseUser> contacts) {
		Log.d(Application.APPTAG, "initializeList!");
		// Set up the list fragment
		list = (ListView) findViewById(R.id.list);
		CustomList adapter = new CustomList(MainActivity.this, contacts);
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
		// Set up the camera change handler
		mapFragment.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				// When the camera changes, update the query
				doMapQuery();
			}
		});
	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in LocationUpdateRemover and LocationUpdateRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.d(Application.APPTAG, "onActivityResult!");
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
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		Log.d(Application.APPTAG, "onConnected!");
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
		Log.d(Application.APPTAG, "onDisconnected!");
		if (Application.APPDEBUG) {
			Log.d("Disconnected from location services", Application.APPTAG);
		}
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(Application.APPTAG, "onConnectionFailed!");
		// Google Play services can resolve some errors it detects. If the error
		// has a resolution, try
		// sending an Intent to start a Google Play services activity that can
		// resolve error.
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
				&& geoPointFromLocation(location).distanceInKilometersTo(
						geoPointFromLocation(lastLocation)) < 0.01) {
			// If the location hasn't changed by more than 10 meters, ignore it.
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
		// Update map radius indicator
		updateCircle(myLatLng);
		doMapQuery();
		doListQuery();
	}

	/*
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {
		Log.d(Application.APPTAG, "startPeriodicdUpdates!");
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	/*
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		Log.d(Application.APPTAG, "stopPeriodicUpdates!");
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
	 * Set up a query to update the list view
	 */
	private void doListQuery() {
		Log.d(Application.APPTAG, "doListQuery!");
		Location myLoc = (currentLocation == null) ? lastLocation
				: currentLocation;
		// If location info is available, load the data
		if (myLoc != null) {
			// Refreshes the list view with new data based
			// usually on updated location data.
			userQueryAdapter.loadObjects();
		}
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
			cleanUpMarkers(new HashSet<String>());
			return;
		}

		final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
		// Create the map Parse query
		ParseQuery<ParseUser> mapQuery = ParseUser.getQuery();
		// Set up additional query filters
		mapQuery.whereWithinMiles("location", myPoint, MAX_POST_SEARCH_DISTANCE);
		mapQuery.include("user");
		mapQuery.orderByDescending("createdAt");
		mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
		// Kick off the query in the background
		mapQuery.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				if (e != null) {
					if (Application.APPDEBUG) {
						Log.d(Application.APPTAG,
								"An error occurred while querying for map posts.",
								e);
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

				// Load Contacts
				List<ParseUser> contacts = objects;
				if (contacts != null) {
					initializeList(contacts);
				}

				// Posts to show on the map
				Set<String> toKeep = new HashSet<String>();
				// Loop through the results of the search
				for (ParseUser user : objects) {
					// Add this post to the list of map pins to keep
					toKeep.add(user.getObjectId());
					// Check for an existing marker for this post
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
						// Display a red marker with a predefined title and no
						// snippet
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
						// Display a green marker with the post information
						markerOpts = markerOpts
								.title(user.getUsername())
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					}
					// Add a new marker
					Marker marker = mapFragment.addMarker(markerOpts);
					mapMarkers.put(user.getObjectId(), marker);
					if (user.getObjectId().equals(selectedPostObjectId)) {
						marker.showInfoWindow();
						selectedPostObjectId = null;
					}
				}
				// Clean up old markers.
				cleanUpMarkers(toKeep);
			}
		});
	}

	/*
	 * Helper method to clean up old markers
	 */
	private void cleanUpMarkers(Set<String> markersToKeep) {
		Log.d(Application.APPTAG, "cleanUpMarkers!");
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			if (!markersToKeep.contains(objId)) {
				Marker marker = mapMarkers.get(objId);
				marker.remove();
				mapMarkers.get(objId).remove();
				mapMarkers.remove(objId);
			}
		}
	}

	/*
	 * Helper method to get the Parse GEO point representation of a location
	 */
	private ParseGeoPoint geoPointFromLocation(Location loc) {
		Log.d(Application.APPTAG, "geoPointFromLocation!");
		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
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
		// Get the bounds to zoom to
		bounds = calculateBoundsWithCenter(myLatLng);
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
	 * Helper method to calculate the offset for the bounds used in map zooming
	 */
	private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
		Log.d(Application.APPTAG, "calculateLatLngOffset!");
		// The return offset, initialized to the default difference
		double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
		// Set up the desired offset distance in meters
		float desiredOffsetInMeters = radius;
		// Variables for the distance calculation
		float[] distance = new float[1];
		boolean foundMax = false;
		double foundMinDiff = 0;
		// Loop through and get the offset
		do {
			// Calculate the distance between the point of interest
			// and the current offset in the latitude or longitude direction
			if (bLatOffset) {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude + latLngOffset, myLatLng.longitude,
						distance);
			} else {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude, myLatLng.longitude + latLngOffset,
						distance);
			}
			// Compare the current difference with the desired one
			float distanceDiff = distance[0] - desiredOffsetInMeters;
			if (distanceDiff < 0) {
				// Need to catch up to the desired distance
				if (!foundMax) {
					foundMinDiff = latLngOffset;
					// Increase the calculated offset
					latLngOffset *= 2;
				} else {
					double tmp = latLngOffset;
					// Increase the calculated offset, at a slower pace
					latLngOffset += (latLngOffset - foundMinDiff) / 2;
					foundMinDiff = tmp;
				}
			} else {
				// Overshot the desired distance
				// Decrease the calculated offset
				latLngOffset -= (latLngOffset - foundMinDiff) / 2;
				foundMax = true;
			}
		} while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
		return latLngOffset;
	}

	/*
	 * Helper method to calculate the bounds for map zooming
	 */
	LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
		Log.d(Application.APPTAG, "calculateBoundsWithCenter!");
		// Create a bounds
		LatLngBounds.Builder builder = LatLngBounds.builder();

		// Calculate east/west points that should to be included
		// in the bounds
		double lngDifference = calculateLatLngOffset(myLatLng, false);
		LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude
				+ lngDifference);
		builder.include(east);
		LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude
				- lngDifference);
		builder.include(west);

		// Calculate north/south points that should to be included
		// in the bounds
		double latDifference = calculateLatLngOffset(myLatLng, true);
		LatLng north = new LatLng(myLatLng.latitude + latDifference,
				myLatLng.longitude);
		builder.include(north);
		LatLng south = new LatLng(myLatLng.latitude - latDifference,
				myLatLng.longitude);
		builder.include(south);

		return builder.build();
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

	/*
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/*
		 * Set the dialog to display
		 * 
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
