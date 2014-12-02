package com.example.communityanimator.scenarios;

import java.util.ArrayList;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.communityanimator.R;
import com.example.communityanimator.util.Application;

public class InterestExpandableListActivity extends ExpandableListActivity {

	// Initialize variables
	private static final String STR_CHECKED = " has Checked!";
	private static final String STR_UNCHECKED = " has unChecked!";
	private int ParentClickStatus = -1;
	private int ChildClickStatus = -1;
	private ArrayList<?> parents;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set ExpandableListView values
		getExpandableListView().setGroupIndicator(null);

		// Creating static data in arraylist
		final ArrayList<?> interestList = buildData();

		// Adding ArrayList data to ExpandableListView values
		loadHosts(interestList);
	}

	/**
	 * here should come your data service implementation
	 * 
	 * @return
	 */
	private ArrayList<InterestParent<?>> buildData() {
		// Creating ArrayList of type parent class to store parent class objects
		final ArrayList<InterestParent<?>> list = new ArrayList<InterestParent<?>>();
		for (int i = 1; i < 18; i++) {
			// Create parent class object
			final InterestParent<InterestChild> parent = new InterestParent<InterestChild>();

			// Set values in parent class object
			if (i == 1) {
				parent.setText("Agriculture and Food");
				parent.setChildren(new ArrayList<InterestChild>());

				// Create Child class object
				final InterestChild child = new InterestChild();
				child.setText("Cooking");
				parent.getChildren().add(child);
				final InterestChild child1 = new InterestChild();
				child1.setText("Local farm co-ops");
				parent.getChildren().add(child1);
			} else if (i == 2) {
				parent.setText("Arts and Culture");
			} else if (i == 3) {
				parent.setText("Built Enviroment");
			} else if (i == 4) {
				parent.setText("Civic Engagement");
				parent.setChildren(new ArrayList<InterestChild>());
				// Create Child class object
				final InterestChild child = new InterestChild();
				child.setText("Volunteerism");
				parent.getChildren().add(child);
			} else if (i == 5) {
				parent.setText("Bussiness/Economy");
			} else if (i == 6) {
				parent.setText("Education and Learning");
				parent.setChildren(new ArrayList<InterestChild>());
				final InterestChild child = new InterestChild();
				child.setText("Tutoring");
				parent.getChildren().add(child);
			} else if (i == 7) {
				parent.setText("Energy");
			} else if (i == 8) {
				parent.setText("Entrepreneuriship");
			} else if (i == 9) {
				parent.setText("Health and Wellness");
			} else if (i == 10) {
				parent.setText("Politics");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Justice and Equality");
				parent.getChildren().add(child);
			} else if (i == 11) {
				parent.setText("Technology");
			} else if (i == 12) {
				parent.setText("Transportation(and Parking)");
			} else if (i == 13) {
				parent.setText("Community Events");
			} else if (i == 14) {
				parent.setText("Community Projects");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Lunar Lion");
				parent.getChildren().add(child);
			} else if (i == 15) {
				parent.setText("Recreation and Leisure");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Local Recreational Sports");
				parent.getChildren().add(child);
				final InterestChild child1 = new InterestChild();
				child1.setText("Climbing");
				parent.getChildren().add(child1);
				final InterestChild child2 = new InterestChild();
				child2.setText("Biking");
				parent.getChildren().add(child2);
				final InterestChild child3 = new InterestChild();
				child3.setText("Kayaking");
				parent.getChildren().add(child3);
				final InterestChild child4 = new InterestChild();
				child4.setText("Running");
				parent.getChildren().add(child4);
				final InterestChild child5 = new InterestChild();
				child5.setText("Hiking");
				parent.getChildren().add(child5);
				final InterestChild child6 = new InterestChild();
				child6.setText("Senior Sports");
				parent.getChildren().add(child6);
				final InterestChild child7 = new InterestChild();
				child7.setText("Writers and Bloggers");
				parent.getChildren().add(child7);
				final InterestChild child8 = new InterestChild();
				child8.setText("Traveling");
				parent.getChildren().add(child8);
				final InterestChild child9 = new InterestChild();
				child9.setText("Gaming");
				parent.getChildren().add(child9);
			} else if (i == 16) {
				parent.setText("Community Groups");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Family/Play Groups");
				parent.getChildren().add(child);
				final InterestChild child1 = new InterestChild();
				child1.setText("Seniors");
				parent.getChildren().add(child1);
				final InterestChild child2 = new InterestChild();
				child2.setText("Veterans");
				parent.getChildren().add(child2);
				final InterestChild child3 = new InterestChild();
				child3.setText("Immigrants in local area");
				parent.getChildren().add(child3);
			} else if (i == 17) {
				parent.setText("Nature, enviroment climate change");
			}

			// Adding Parent class object to ArrayList
			list.add(parent);
		}
		return list;
	}

	private void loadHosts(final ArrayList<?> newParents) {
		if (newParents == null)
			return;

		parents = newParents;

		// Check for ExpandableListAdapter object
		if (this.getExpandableListAdapter() == null) {
			// Create ExpandableListAdapter Object
			final MyExpandableListAdapter mAdapter = new MyExpandableListAdapter();

			// Set Adapter to ExpandableList Adapter
			this.setListAdapter(mAdapter);
		} else {
			// Refresh ExpandableListView data
			((MyExpandableListAdapter) getExpandableListAdapter())
					.notifyDataSetChanged();
		}
	}

	/**
	 * A Custom adapter to create Parent view (Used grouprow.xml) and Child
	 * View((Used childrow.xml).
	 */
	private class MyExpandableListAdapter extends BaseExpandableListAdapter {

		private LayoutInflater inflater;

		public MyExpandableListAdapter() {
			// Create Layout Inflator
			inflater = LayoutInflater.from(InterestExpandableListActivity.this);
		}

		// This Function used to inflate parent rows view

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parentView) {
			final InterestParent<?> parent = (InterestParent<?>) parents
					.get(groupPosition);

			// Inflate grouprow.xml file for parent rows
			convertView = inflater.inflate(R.layout.grouprow_list, parentView,
					false);

			// Get grouprow.xml file elements and set values
			((TextView) convertView.findViewById(R.id.text1)).setText(parent
					.getText());
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			if (isExpanded) {
				image.setImageResource(R.drawable.ic_down);
			} else {
				image.setImageResource(R.drawable.ic_right);
			}

			// Get grouprow.xml file checkbox elements
			CheckBox checkbox = (CheckBox) convertView
					.findViewById(R.id.checkbox);
			checkbox.setChecked(parent.isChecked());

			// Set CheckUpdateListener for CheckBox (see below
			// CheckUpdateListener class)
			checkbox.setOnCheckedChangeListener(new CheckUpdateListener(parent));

			return convertView;
		}

		// This Function used to inflate child rows view
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parentView) {
			final InterestParent<?> parent = (InterestParent<?>) parents
					.get(groupPosition);
			final InterestChild child = (InterestChild) parent.getChildren()
					.get(childPosition);

			// Inflate childrow.xml file for child rows
			convertView = inflater.inflate(R.layout.childrow_list, parentView,
					false);

			// Get childrow.xml file elements and set values
			((TextView) convertView.findViewById(R.id.text1)).setText(child
					.getText());

			return convertView;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
			return ((InterestParent<?>) parents.get(groupPosition))
					.getChildren().get(childPosition);
		}

		// Call when child row clicked
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			/****** When Child row clicked then this function call *******/

			Log.i(Application.APPTAG, "parent == " + groupPosition
					+ "=  child : ==" + childPosition);

			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int size = 0;
			if (((InterestParent<?>) parents.get(groupPosition)).getChildren() != null)
				size = ((InterestParent<?>) parents.get(groupPosition))
						.getChildren().size();
			return size;
		}

		@Override
		public Object getGroup(int groupPosition) {
			Log.i(Application.APPTAG, groupPosition + "=  getGroup ");

			return parents.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return parents.size();
		}

		// Call when parent row clicked
		@Override
		public long getGroupId(int groupPosition) {
			Log.i(Application.APPTAG, groupPosition + "=  getGroupId "
					+ ParentClickStatus);

			ParentClickStatus = groupPosition;
			if (ParentClickStatus == 0)
				ParentClickStatus = -1;

			return groupPosition;
		}

		@Override
		public void notifyDataSetChanged() {
			// Refresh List rows
			super.notifyDataSetChanged();
		}

		@Override
		public boolean isEmpty() {
			return ((parents == null) || parents.isEmpty());
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		/******************* Checkbox Checked Change Listener ********************/

		private final class CheckUpdateListener implements
				OnCheckedChangeListener {
			private final InterestParent<?> parent;

			private CheckUpdateListener(InterestParent<?> parent) {
				this.parent = parent;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(Application.APPTAG, "isChecked: " + isChecked);
				parent.setChecked(isChecked);

				((MyExpandableListAdapter) getExpandableListAdapter())
						.notifyDataSetChanged();

				// final Boolean checked = parent.isChecked();
				// Toast.makeText(
				// getApplicationContext(),
				// "Parent : " + parent.getText() + " "
				// + (checked ? STR_CHECKED : STR_UNCHECKED),
				// Toast.LENGTH_LONG).show();
			}
		}
		/***********************************************************************/

	}
}
