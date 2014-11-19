package com.example.communityanimator;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.communityanimator.database.User;

public class CustomListAdapter extends BaseAdapter {

	LayoutInflater mInflater;
	private List<User> user = null;
	Context mContext;

	public CustomListAdapter(Context context, List<User> objects) {
		this.mContext = context;
		this.user = objects;
		mInflater = LayoutInflater.from(mContext);

		Log.d("Adapter", "list size:" + objects.size());
	}

	public class ViewHolder {
		TextView txtName;
		TextView txtStatus;
		ImageView image;
		ImageView add;
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
		final ViewHolder holder;

		if (view == null) {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.contact_item, null);
			holder.txtName = (TextView) view.findViewById(R.id.contactName);
			holder.txtStatus = (TextView) view.findViewById(R.id.contactStatus);
			holder.image = (ImageView) view.findViewById(R.id.imgContact);
			holder.add = (ImageView) view.findViewById(R.id.addContact);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.txtName.setText(user.get(position).getUsername());
		Log.d("Adapter", "username: " + user.get(position).getUsername());
		boolean status = user.get(position).getStatus();
		if (status) {
			holder.txtStatus.setText("animated");
		} else {
			holder.txtStatus.setText("busy");
		}

		boolean chatting = user.get(position).getChatting();
		Log.d("Adapter", "chatting: " + chatting);
		if (chatting) {
			holder.add.setImageResource(R.drawable.ic_chat);
		} else {
			holder.add.setImageResource(R.drawable.ic_plus);
		}

		byte[] data;
		Bitmap newBmp = null;
		data = user.get(position).getPhotoFile();
		if (data != null) {
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			newBmp = findDensity(bmp);
			holder.image.setImageBitmap(newBmp);
		} else {
			holder.image.setImageResource(R.drawable.contact);
		}

		return view;
	}

	private Bitmap findDensity(Bitmap bmp) {
		Bitmap obj = null;
		int density = mContext.getResources().getDisplayMetrics().densityDpi;
		switch (density) {
		case DisplayMetrics.DENSITY_MEDIUM:
			obj = ThumbnailUtils.extractThumbnail(bmp, 48, 48);
			break;
		case DisplayMetrics.DENSITY_HIGH:
			obj = ThumbnailUtils.extractThumbnail(bmp, 72, 72);
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			obj = ThumbnailUtils.extractThumbnail(bmp, 96, 96);
			break;
		}
		return obj;
	}
}
