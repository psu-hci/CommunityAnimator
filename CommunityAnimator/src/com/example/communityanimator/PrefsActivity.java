package com.example.communityanimator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.parse.ParseUser;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		Preference logout = findPreference("logout");
		logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				ParseUser.getCurrentUser();
				ParseUser.logOut();
				Intent intent = new Intent(PrefsActivity.this, Login.class);
				startActivity(intent);
				PrefsActivity.this.finish();
				return true;
			}
		});
	}

}
