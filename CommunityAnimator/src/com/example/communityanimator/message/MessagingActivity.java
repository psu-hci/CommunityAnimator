package com.example.communityanimator.message;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.communityanimator.R;
import com.example.communityanimator.util.Application;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessagingActivity extends Activity {

	private String recipientId;
	private EditText messageBodyField;
	private String messageBody;
	private MessageService.MessageServiceInterface messageService;
	private MessageAdapter messageAdapter;
	private ListView messagesList;
	private String currentUserId;
	private ServiceConnection serviceConnection = new MyServiceConnection();
	private MessageClientListener messageClientListener = new MyMessageClientListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messaging);

		bindService(new Intent(this, MessageService.class), serviceConnection,
				BIND_AUTO_CREATE);

		Intent intent = getIntent();
		recipientId = intent.getStringExtra("RECIPIENT_ID");
		currentUserId = ParseUser.getCurrentUser().getObjectId();

		messagesList = (ListView) findViewById(R.id.listMessages);
		messageAdapter = new MessageAdapter(this);
		messagesList.setAdapter(messageAdapter);
		populateMessageHistory();

		messageBodyField = (EditText) findViewById(R.id.messageBodyField);

		findViewById(R.id.sendButton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						sendMessage();
					}
				});
	}

	// get previous messages from parse & display
	private void populateMessageHistory() {
		String[] userIds = { currentUserId, recipientId };
		ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
		query.whereContainedIn("senderId", Arrays.asList(userIds));
		query.whereContainedIn("recipientId", Arrays.asList(userIds));
		query.orderByAscending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> messageList,
					com.parse.ParseException e) {
				if (e == null) {
					for (int i = 0; i < messageList.size(); i++) {
						WritableMessage message = new WritableMessage(
								messageList.get(i).get("recipientId")
										.toString(), messageList.get(i)
										.get("messageText").toString());
						if (messageList.get(i).get("senderId").toString()
								.equals(currentUserId)) {
							messageAdapter.addMessage(message,
									MessageAdapter.DIRECTION_OUTGOING);
						} else {
							messageAdapter.addMessage(message,
									MessageAdapter.DIRECTION_INCOMING);
						}
					}
				}
			}
		});
	}

	private void sendMessage() {
		messageBody = messageBodyField.getText().toString();
		Log.d(Application.APPTAG, "messageBody: " + messageBody);
		if (messageBody.isEmpty()) {
			Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG)
					.show();
			return;
		}

		Log.d(Application.APPTAG, "recipientId: " + recipientId);
		Log.d(Application.APPTAG, "messageBody: " + messageBody);
		messageService.isSinchClientStarted();
		messageService.sendMessage(recipientId, messageBody);
		messageBodyField.setText("");
	}

	@Override
	public void onDestroy() {
		messageService.removeMessageClientListener(messageClientListener);
		unbindService(serviceConnection);
		super.onDestroy();
	}

	private class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			messageService = (MessageService.MessageServiceInterface) iBinder;
			messageService.addMessageClientListener(messageClientListener);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			messageService = null;
		}
	}

	public class MyMessageClientListener implements MessageClientListener {

		@Override
		public void onIncomingMessage(MessageClient client, Message message) {

			if (message.getSenderId().equals(recipientId)) {
				WritableMessage writableMessage = new WritableMessage(message
						.getRecipientIds().get(0), message.getTextBody());
				messageAdapter.addMessage(writableMessage,
						MessageAdapter.DIRECTION_INCOMING);
			}
		}

		@Override
		public void onMessageDelivered(MessageClient client,
				MessageDeliveryInfo deliveryInfo) {

			Log.d(Application.APPTAG,
					"The message with id " + deliveryInfo.getMessageId()
							+ " was delivered to the recipient with id"
							+ deliveryInfo.getRecipientId());
		}

		@Override
		public void onMessageFailed(MessageClient arg0, Message arg1,
				MessageFailureInfo arg2) {

			Toast.makeText(MessagingActivity.this, "Message failed to send.",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onMessageSent(MessageClient client, Message message,
				String recipientId) {

			final WritableMessage writableMessage = new WritableMessage(message
					.getRecipientIds().get(0), message.getTextBody());

			// only add message to parse database if it doesn't already exist
			// there
			ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
			query.whereEqualTo("sinchId", message.getMessageId());
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> messageList,
						com.parse.ParseException e) {
					if (e == null) {
						if (messageList.size() == 0) {
							ParseObject parseMessage = new ParseObject(
									"ParseMessage");
							parseMessage.put("senderId", currentUserId);
							parseMessage.put("recipientId", writableMessage
									.getRecipientIds().get(0));
							parseMessage.put("messageText",
									writableMessage.getTextBody());
							parseMessage.put("sinchId",
									writableMessage.getMessageId());
							parseMessage.saveInBackground();

							messageAdapter.addMessage(writableMessage,
									MessageAdapter.DIRECTION_OUTGOING);
						}
					}
				}
			});
		}

		@Override
		public void onShouldSendPushData(MessageClient client, Message msg,
				List<PushPair> push) {

			Log.d(Application.APPTAG, "client: " + client.toString());
			Log.d(Application.APPTAG, "msg: " + msg.toString());
			// Log.d(Application.APPTAG, "List<PushPair> : " + arg2.);
			// String message = "Start messaging now!";
			// // Create Installation query
			// ParseQuery<ParseInstallation> pushQuery = ParseInstallation
			// .getQuery();
			// pushQuery.whereEqualTo("user", client);
			//
			// // Send push notification to query
			// ParsePush push = new ParsePush();
			// push.setQuery(pushQuery);
			// push.setMessage(message);
			// push.sendInBackground();

		}
	}

}
