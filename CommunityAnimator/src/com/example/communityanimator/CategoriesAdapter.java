package com.example.communityanimator;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class CategoriesAdapter extends ArrayAdapter<Categories> {

	private ArrayList<Categories> countryList;
	Context mContext;

	public CategoriesAdapter(Context context, int textViewResourceId,
			ArrayList<Categories> countryList) {
		super(context, textViewResourceId, countryList);
		this.mContext = context;
		this.countryList = new ArrayList<Categories>();
		this.countryList.addAll(countryList);
	}

	private class ViewHolder {
		TextView name;
		CheckBox code;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.interest_items, null);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.code);
			holder.code = (CheckBox) convertView.findViewById(R.id.checkBox1);
			convertView.setTag(holder);

			holder.code.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					Categories country = (Categories) cb.getTag();
					Toast.makeText(
							mContext,
							"Clicked on Checkbox: " + cb.getText() + " is "
									+ cb.isChecked(), Toast.LENGTH_LONG).show();
					country.setSelected(cb.isChecked());
				}
			});
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Categories country = countryList.get(position);
		holder.name.setText(country.getName());
		holder.code.setChecked(country.isSelected());

		return convertView;

	}

}
