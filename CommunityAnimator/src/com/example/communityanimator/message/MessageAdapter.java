package com.example.communityanimator.message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.communityanimator.R;
import com.example.communityanimator.util.Application;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageAdapter extends BaseAdapter {

	public static final int DIRECTION_INCOMING = 0;
	public static final int DIRECTION_OUTGOING = 1;

	private List<Pair<WritableMessage, Integer>> messages;
	private LayoutInflater layoutInflater;
	private String currentUsername = ParseUser.getCurrentUser().getUsername();
	private String recipientName, recipientId;

	public MessageAdapter(Activity activity, String recipientID) {
		layoutInflater = activity.getLayoutInflater();
		messages = new ArrayList<Pair<WritableMessage, Integer>>();
		this.recipientId = recipientID;
		findWriterName(recipientId);
	}

	public void addMessage(WritableMessage message, int direction) {
		messages.add(new Pair<WritableMessage, Integer>(message, direction));
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int i) {
		return messages.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int i) {
		return messages.get(i).second;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(int i, View convertView, ViewGroup viewGroup) {
		int direction = getItemViewType(i);

		// show message on left or right, depending on if
		// it's incoming or outgoing
		if (convertView == null) {
			int res = 0;
			if (direction == DIRECTION_INCOMING) {
				res = R.layout.message_right;
			} else if (direction == DIRECTION_OUTGOING) {
				res = R.layout.message_left;
			}
			convertView = layoutInflater.inflate(res, viewGroup, false);
		}

		WritableMessage message = messages.get(i).first;

		TextView txtMessage = (TextView) convertView
				.findViewById(R.id.txtMessage);
		txtMessage.setText(message.getTextBody());

		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		String currentDateandTime = sdf.format(new Date());
		TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
		txtDate.setText(currentDateandTime);

		TextView txtSender = (TextView) convertView
				.findViewById(R.id.txtSender);
		if (direction == DIRECTION_INCOMING) {
			txtSender.setText(recipientName);
		} else if (direction == DIRECTION_OUTGOING) {
			txtSender.setText(currentUsername);
		}

		return convertView;
	}

	private void findWriterName(String writerId) {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("objectId", writerId);
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					recipientName = user.getUsername();
				} else {
					Log.d(Application.APPTAG,
							"An error occurred while querying.", e);
				}

			}
		});
	}
}
