package com.example.communityanimator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.parse.ParseUser;

public class PrefsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		// Get instance of Vibrator from current Context
		final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		Preference logout = findPreference("logout");
		logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						PrefsActivity.this);
				builder.setTitle(R.string.logOutTitle)
						.setMessage(R.string.LogOutMessage)
						.setCancelable(false)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										ParseUser.getCurrentUser();
										ParseUser.logOut();
										Intent intent = new Intent(
												PrefsActivity.this, Login.class);
										startActivity(intent);
										PrefsActivity.this.finish();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});

		Preference about = findPreference("about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				showAbout();
				return true;
			}
		});

		Preference faq = findPreference("faq");
		faq.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				showFAQ();
				return true;
			}
		});

		Preference feedback = findPreference("contactUs");
		feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				showFeedback();
				return true;
			}
		});

		SwitchPreference vibrate = (SwitchPreference) findPreference("vibrate");
		vibrate.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {

				boolean switched = ((SwitchPreference) preference).isChecked();

				if (switched) {
					long[] pattern = { 0, 100, 1000 };
					v.vibrate(pattern, -1);
				} else {
					v.cancel();
				}
				return true;
			}
		});

		// Set notification
		bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));

	}

	protected void showAbout() {
		// Inflate the about message contents
		View messageView = getLayoutInflater().inflate(R.layout.about_dialog,
				null, false);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.aboutDialog);
		builder.setView(messageView);
		builder.create();
		builder.show();
	}

	protected void showFAQ() {
		// Inflate the faq contents
		View faqView = getLayoutInflater().inflate(R.layout.faq_dialog, null,
				false);
		WebView faq = (WebView) faqView.findViewById(R.id.faqView);

		String helpText = readRawTextFile(R.raw.faq_contents);
		faq.loadData(helpText, "text/html; charset=utf-8", "utf-8");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.faqTitle);
		builder.setView(faqView);
		builder.create();
		builder.show();

	}

	protected void showFeedback() {
		// Inflate the feedback contact contents, using the same view of FAQ
		View faqView = getLayoutInflater().inflate(R.layout.faq_dialog, null,
				false);
		WebView feedback = (WebView) faqView.findViewById(R.id.faqView);
		feedback.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("mailto:")) {
					MailTo mt = MailTo.parse(url);
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_EMAIL,
							new String[] { mt.getTo() });
					intent.putExtra(Intent.EXTRA_SUBJECT,
							"Community Animator feedback");

					String title = getResources().getString(
							R.string.action_websearch);
					// Create intent to show chooser
					Intent chooser = Intent.createChooser(intent, title);

					// Verify the intent will resolve to at least one activity
					if (intent.resolveActivity(getPackageManager()) != null) {
						startActivity(chooser); // TODO: verify chooser
					}
					return true;
				} else {
					view.loadUrl(url);
				}
				return true;
			}
		});

		String helpText = readRawTextFile(R.raw.feedback_contents);
		feedback.loadData(helpText, "text/html; charset=utf-8", "utf-8");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.contactUsTitle);
		builder.setView(faqView);
		builder.create();
		builder.show();

	}

	private String readRawTextFile(int id) {
		InputStream inputStream = PrefsActivity.this.getResources()
				.openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try {
			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.ringtoneSilent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}
}
