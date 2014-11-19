package com.example.communityanimator;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.communityanimator.message.MessagingActivity;
import com.example.communityanimator.util.Application;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class NotificationActivity extends Activity {

	String sender, receiver;
	private ProgressDialog progressDialog;
	private BroadcastReceiver receiverBroadcast = null;
	String data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ParseAnalytics.trackAppOpened(getIntent());
		Intent intent = getIntent();
		JSONObject json;
		try {
			json = new JSONObject(intent.getExtras()
					.getString("com.parse.Data"));
			Iterator itr = json.keys();

			while (itr.hasNext()) {
				String key = (String) itr.next();
				if (key.equals("customdata")) {

					data = json.getString(key);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		StringTokenizer tokens = new StringTokenizer(data, "/");
		final String sender = tokens.nextToken();
		final String receiver = tokens.nextToken();

		AlertDialog.Builder alertDialogStatus = new AlertDialog.Builder(
				NotificationActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		View notificationView = inflater.inflate(R.layout.notification_dialog,
				null);

		alertDialogStatus.setView(notificationView);
		alertDialogStatus.setTitle("Notification");

		final TextView question = (TextView) notificationView
				.findViewById(R.id.txtquestion);

		question.setText("Do you want to chat with " + sender + " ?");
		final AlertDialog dialog = alertDialogStatus.create();
		dialog.show();

		Button statusOk = (Button) notificationView
				.findViewById(R.id.btn_Accept);
		statusOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				showSpinner();
				ParseQuery<ParseUser> receiverQuery = ParseUser.getQuery();
				receiverQuery.whereEqualTo("username", sender);
				receiverQuery.findInBackground(new FindCallback<ParseUser>() {

					@Override
					public void done(List<ParseUser> object, ParseException e) {
						if (e == null) {

							Intent intent = new Intent(
									NotificationActivity.this,
									MessagingActivity.class);
							intent.putExtra("RECIPIENT_ID", object.get(0)
									.getObjectId());
							startActivity(intent);

						} else {
							Log.d(Application.APPTAG,
									"An error occurred while querying.", e);
						}
					}
				});
			}
		});

		Button statusCancel = (Button) notificationView
				.findViewById(R.id.btn_Refuse);
		statusCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String message = "Your friend request was refused by "
						+ receiver;
				// Create Installation query
				ParseQuery<ParseInstallation> pushQuery = ParseInstallation
						.getQuery();
				pushQuery.whereEqualTo("user", sender);

				// Send push notification to query
				ParsePush push = new ParsePush();
				push.setQuery(pushQuery); // Set our Installation query
				push.setMessage(message);
				push.sendInBackground();

				Intent i = new Intent(NotificationActivity.this,
						MainActivity.class);
				startActivity(i);
			}
		});

	}

	// show a loading spinner while the sinch client starts
	private void showSpinner() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Please wait...");
		progressDialog.show();

		receiverBroadcast = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Boolean success = intent.getBooleanExtra("success", false);
				progressDialog.dismiss();
				if (!success) {
					Toast.makeText(getApplicationContext(),
							"Messaging service failed to start",
							Toast.LENGTH_LONG).show();
				}
			}
		};

		LocalBroadcastManager.getInstance(this).registerReceiver(
				receiverBroadcast,
				new IntentFilter(
						"com.example.communityanimator.NotificationActivity"));
	}
}
