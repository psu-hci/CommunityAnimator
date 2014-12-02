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
import android.widget.Toast;

import com.example.communityanimator.R;

public class TaskExpandableListActivity extends ExpandableListActivity {

	// Initialize variables
	private static final String STR_CHECKED = " has Checked!";
	private static final String STR_UNCHECKED = " has unChecked!";
	private int ParentClickStatus = -1;
	private int ChildClickStatus = -1;
	private ArrayList<InterestParent<?>> parents;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set ExpandableListView values
		getExpandableListView().setGroupIndicator(null);

		// Creating static data in arraylist
		final ArrayList<InterestParent<?>> dummyList = buildDummyData();

		// Adding ArrayList data to ExpandableListView values
		loadHosts(dummyList);
	}

	/**
	 * here should come your data service implementation
	 * 
	 * @return
	 */
	private ArrayList<InterestParent<?>> buildDummyData() {
		// Creating ArrayList of type parent class to store parent class objects
		final ArrayList<InterestParent<?>> list = new ArrayList<InterestParent<?>>();
		for (int i = 1; i < 4; i++) {
			// Create parent class object
			final InterestParent<InterestChild> parent = new InterestParent<InterestChild>();

			// Set values in parent class object
			if (i == 1) {
				parent.setText("Parent 0");
				parent.setChildren(new ArrayList<InterestChild>());

				// Create Child class object
				final InterestChild child = new InterestChild();
				child.setText("Child 0");

				// Add Child class object to parent class object
				parent.getChildren().add(child);
			} else if (i == 2) {
				parent.setText("Parent 1");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Child 0");
				parent.getChildren().add(child);
				final InterestChild child1 = new InterestChild();
				child1.setText("Child 1");
				parent.getChildren().add(child1);
			} else if (i == 3) {
				parent.setText("Parent 1");
				parent.setChildren(new ArrayList<InterestChild>());

				final InterestChild child = new InterestChild();
				child.setText("Child 0");
				parent.getChildren().add(child);
				final InterestChild child1 = new InterestChild();
				child1.setText("Child 1");
				parent.getChildren().add(child1);
				final InterestChild child2 = new InterestChild();
				child2.setText("Child 2");
				parent.getChildren().add(child2);
				final InterestChild child3 = new InterestChild();
				child3.setText("Child 3");
				parent.getChildren().add(child3);
			}

			// Adding Parent class object to ArrayList
			list.add(parent);
		}
		return list;
	}

	private void loadHosts(final ArrayList<InterestParent<?>> newParents) {
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
			inflater = LayoutInflater.from(TaskExpandableListActivity.this);
		}

		// This Function used to inflate parent rows view

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parentView) {
			final InterestParent<?> parent = parents.get(groupPosition);

			// Inflate grouprow.xml file for parent rows
			convertView = inflater.inflate(R.layout.grouprow_list, parentView,
					false);

			// Get grouprow.xml file elements and set values
			((TextView) convertView.findViewById(R.id.text1)).setText(parent
					.getText());
			// ((TextView) convertView.findViewById(R.id.text)).setText(parent
			// .getText2());
			ImageView image = (ImageView) convertView.findViewById(R.id.image);

			// ImageView rightcheck = (ImageView) convertView
			// .findViewById(R.id.rightcheck);
			//
			// // Log.i("onCheckedChanged", "isChecked: "+parent.isChecked());
			//
			// // Change right check image on parent at runtime
			// if (parent.isChecked() == true) {
			// rightcheck
			// .setImageResource(getResources()
			// .getIdentifier(
			// "com.androidexample.customexpandablelist:drawable/rightcheck",
			// null, null));
			// } else {
			// rightcheck
			// .setImageResource(getResources()
			// .getIdentifier(
			// "com.androidexample.customexpandablelist:drawable/button_check",
			// null, null));
			// }

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
			final InterestParent<?> parent = parents.get(groupPosition);
			final InterestChild child = (InterestChild) parent.getChildren()
					.get(childPosition);

			// Inflate childrow.xml file for child rows
			convertView = inflater.inflate(R.layout.childrow_list, parentView,
					false);

			// Get childrow.xml file elements and set values
			((TextView) convertView.findViewById(R.id.text1)).setText(child
					.getText());
			ImageView image = (ImageView) convertView.findViewById(R.id.image);

			return convertView;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
			return parents.get(groupPosition).getChildren().get(childPosition);
		}

		// Call when child row clicked
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			/****** When Child row clicked then this function call *******/

			// Log.i("Noise",
			// "parent == "+groupPosition+"=  child : =="+childPosition);
			if (ChildClickStatus != childPosition) {
				ChildClickStatus = childPosition;

				Toast.makeText(
						getApplicationContext(),
						"Parent :" + groupPosition + " Child :" + childPosition,
						Toast.LENGTH_LONG).show();
			}

			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int size = 0;
			if (parents.get(groupPosition).getChildren() != null)
				size = parents.get(groupPosition).getChildren().size();
			return size;
		}

		@Override
		public Object getGroup(int groupPosition) {
			Log.i("Parent", groupPosition + "=  getGroup ");

			return parents.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return parents.size();
		}

		// Call when parent row clicked
		@Override
		public long getGroupId(int groupPosition) {
			Log.i("Parent", groupPosition + "=  getGroupId "
					+ ParentClickStatus);

			if (groupPosition == 2 && ParentClickStatus != groupPosition) {

				// Alert to user
				Toast.makeText(getApplicationContext(),
						"Parent :" + groupPosition, Toast.LENGTH_LONG).show();
			}

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
				Log.i("onCheckedChanged", "isChecked: " + isChecked);
				parent.setChecked(isChecked);

				((MyExpandableListAdapter) getExpandableListAdapter())
						.notifyDataSetChanged();

				final Boolean checked = parent.isChecked();
				Toast.makeText(
						getApplicationContext(),
						"Parent : " + parent.getText() + " "
								+ (checked ? STR_CHECKED : STR_UNCHECKED),
						Toast.LENGTH_LONG).show();
			}
		}
		/***********************************************************************/

	}
}
