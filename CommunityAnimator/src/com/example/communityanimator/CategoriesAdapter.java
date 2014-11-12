package com.example.communityanimator;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class CategoriesAdapter extends ArrayAdapter<Categories> {

	ArrayList<Categories> mCategoriesList;
	Context mContext;
	boolean[] itemChecked;

	public CategoriesAdapter(Context context, int textViewResourceId,
			ArrayList<Categories> categoriesList) {
		super(context, textViewResourceId, categoriesList);
		this.mContext = context;
		this.mCategoriesList = new ArrayList<Categories>();
		this.mCategoriesList.addAll(categoriesList);

		itemChecked = new boolean[mCategoriesList.size()];
	}

	private class ViewHolder {
		TextView name;
		CheckBox check;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.interest_items, null);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.code);
			holder.check = (CheckBox) convertView.findViewById(R.id.checkBox1);
			convertView.setTag(holder);

			holder.check.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// if (holder.check.isChecked())
					// itemChecked[position] = true;
					// else
					// itemChecked[position] = false;

					CheckBox cb = (CheckBox) v;
					Categories c = (Categories) cb.getTag();
					c.setSelected(cb.isChecked());
				}
			});

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Categories c = mCategoriesList.get(position);
		holder.name.setText(c.getName());
		holder.check.setChecked(c.isSelected());
		holder.check.setTag(c);

		// if (itemChecked[position])
		// holder.check.setChecked(true);
		// else
		// holder.check.setChecked(false);

		return convertView;

	}

}
