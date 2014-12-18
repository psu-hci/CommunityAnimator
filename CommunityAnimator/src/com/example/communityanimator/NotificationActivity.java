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
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class NotificationActivity extends Activity {

	String mSender, mReceiver, mObject;
	private ProgressDialog progressDialog;
	private BroadcastReceiver receiverBroadcast = null;
	String data;
	ParseUser mUser = ParseUser.getCurrentUser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Application.APPTAG, "NotificationActivity");

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

		if (data != null) {
			StringTokenizer tokens = new StringTokenizer(data, "/");
			final String sender = tokens.nextToken();
			mSender = sender;
			final String receiver = tokens.nextToken();
			mReceiver = receiver;

			AlertDialog.Builder alertDialogStatus = new AlertDialog.Builder(
					NotificationActivity.this);
			LayoutInflater inflater = getLayoutInflater();
			View notificationView = inflater.inflate(
					R.layout.notification_dialog, null);

			alertDialogStatus.setView(notificationView);
			alertDialogStatus.setTitle("Notification");

			final TextView question = (TextView) notificationView
					.findViewById(R.id.txtquestion);

			question.setText(sender
					+ " has initiated a task with you. Do you wish to accept?");
			final AlertDialog dialog = alertDialogStatus.create();
			dialog.show();

			Button statusOk = (Button) notificationView
					.findViewById(R.id.btn_Accept);
			statusOk.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// who accepts is the new sender
					showSpinner();
					ParseQuery<ParseUser> receiverQuery = ParseUser.getQuery();
					receiverQuery.whereEqualTo("username", sender);
					receiverQuery
							.findInBackground(new FindCallback<ParseUser>() {

								@Override
								public void done(List<ParseUser> object,
										ParseException e) {
									if (e == null) {

										getFriendID();
										mObject = object.get(0).getObjectId();
										Intent intent = new Intent(
												NotificationActivity.this,
												MessagingActivity.class);
										intent.putExtra("RECIPIENT_ID", mObject);
										startActivity(intent);

									} else {
										Log.d(Application.APPTAG,
												"An error occurred while querying.",
												e);
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

					String message = "Your task to " + receiver
							+ " was not accepted at this time.";
					// Create Installation query
					ParseQuery<ParseInstallation> pushQuery = ParseInstallation
							.getQuery();
					pushQuery.whereEqualTo("user", sender);

					// Send push notification to query
					ParsePush push = new ParsePush();
					push.setQuery(pushQuery); // Set our Installation query
					push.setMessage(message);
					push.sendInBackground();

				}
			});
		} else {
			Intent in = new Intent(NotificationActivity.this,
					MainActivity.class);
			startActivity(in);
		}

	}

	private void acceptNotification() {
		Log.d(Application.APPTAG, "AcceptNotification");

		String message = "Your task to " + mReceiver + " was accepted.";
		// Create Installation query
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereEqualTo("user", mSender);

		// Send push notification to query
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery); // Set our Installation query
		push.setMessage(message);
		push.sendInBackground();
	}

	private void updateChatStatus() {
		Log.d(Application.APPTAG, "updateChatStatus!");
		// Update receiver status
		mUser.put("chatting", true);
		mUser.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {

				if (e == null) {
					createFriends();
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}
			}
		});
	}

	public void createFriends() {
		ParseObject friend = new ParseObject("Friends");
		friend.put("sendername", mSender);
		friend.put("recipientname", mReceiver);
		friend.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {

				if (e == null) {
					acceptNotification();
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}
			}
		});
	}

	private void getFriendID() {
		ParseQuery<ParseUser> friendID = ParseUser.getQuery();
		friendID.whereEqualTo("username", mSender);
		friendID.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					mObject = user.getObjectId();
					updateChatStatus();
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}
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
