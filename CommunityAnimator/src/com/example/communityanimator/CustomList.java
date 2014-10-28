package com.example.communityanimator;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.communityanimator.util.Application;
import com.parse.ParseUser;

public class CustomList extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<ParseUser> user;
	Context mContext;

	public CustomList(Context context, List<ParseUser> objects) {
		this.mContext = context;
		this.user = objects;

		Log.d(Application.APPTAG, "adapter objects size: " + user.size());
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		return user.size();
	}

	@Override
	public Object getItem(int position) {
		return user.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View rowView;

		if (view == null) {
			rowView = mInflater.inflate(R.layout.contact_item, null);
		} else {
			rowView = view;
		}

		TextView txtName = (TextView) rowView.findViewById(R.id.contactName);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imgContact);
		TextView txtStatus = (TextView) rowView
				.findViewById(R.id.contactStatus);

		ParseUser mUser = user.get(position);

		txtName.setText(mUser.getUsername());
		// Object img = mUser.getParseFile("image");
		// if (img.equals(null)) {
		// imageView.setImageResource(R.drawable.contact);
		// }

		boolean status = mUser.getBoolean("status");
		if (status) {
			txtStatus.setText("animated");
		} else {
			txtStatus.setText("busy");
		}

		return rowView;
	}

}
