package com.example.communityanimator.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;

public class Application extends android.app.Application {

	// Debugging switch
	public static final boolean APPDEBUG = false;

	// Debugging tag for the application
	public static final String APPTAG = "CommunityAnimator";

	// Used to pass location from MainActivity to PostActivity
	public static final String INTENT_EXTRA_LOCATION = "location";

	// Key for saving the search distance preference
	private static final String KEY_SEARCH_DISTANCE = "searchDistance";

	private static final float DEFAULT_SEARCH_DISTANCE = 50.0f; // peer feet

	private static SharedPreferences preferences;

	private static ConfigHelper configHelper;

	public Application() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		FacebookSdk.sdkInitialize(getApplicationContext());

		ParseCrashReporting.enable(this);

		Parse.initialize(this, "T4lD84ZeLY7615h43jpGlVTG5cXZyXd8ceSGX29e",
				"NksRHt7K9ldAmmfVUq843DY4mmWuUQRaQWecvcxa");

		ParseFacebookUtils.initialize(this);

		// Specify an Activity to handle all pushes by default.
		// PushService.setDefaultPushCallback(this, NotificationActivity.class);

		preferences = getSharedPreferences("com.example.communityanimator",
				Context.MODE_PRIVATE);

		configHelper = new ConfigHelper();
		configHelper.fetchConfigIfNeeded();

		// Test ParseCrashReporting functionality
		// throw new RuntimeException("Test Exception!");
	}

	public static float getSearchDistance() {
		return preferences.getFloat(KEY_SEARCH_DISTANCE,
				DEFAULT_SEARCH_DISTANCE);
	}

	public static ConfigHelper getConfigHelper() {
		return configHelper;
	}

	public static void setSearchDistance(float value) {
		preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
	}

}
