package com.example.communityanimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

public class Receiver extends ParsePushBroadcastReceiver {

	@Override
	public void onPushOpen(Context context, Intent intent) {
		Intent i = new Intent(context, NotificationActivity.class);
		i.putExtras(intent.getExtras());
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

	@Override
	protected Class<? extends Activity> getActivity(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		return NotificationActivity.class;
	}

}
