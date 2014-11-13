package com.example.communityanimator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.MailTo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
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
}
