package com.example.communityanimator.scenarios;

import java.util.ArrayList;
import java.util.List;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.communityanimator.R;
import com.example.communityanimator.SignUp;
import com.example.communityanimator.util.Application;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class TaskExpandableListActivity extends ExpandableListActivity {

	// Initialize variables
	private int ParentClickStatus = -1;
	private ArrayList<TaskParent<?>> parents;
	List<ParseObject> ob;
	ProgressDialog mProgressDialog;
	boolean viewProfile = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expandablelist);

		TextView title = (TextView) findViewById(R.id.textTitle);
		title.setText(R.string.taskList);

		Button saveChoice = (Button) findViewById(R.id.btn_SaveChoices);
		saveChoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Using a List to hold the IDs, but could use an array.
				ArrayList<String> checkedIDs = new ArrayList<String>();

				for (int i = 0; i < parents.size(); i++) {
					TaskParent<?> parent = parents.get(i);

					if (parent.isChecked()) {
						// Put the value of the id in our list
						String id = ob.get(i).getObjectId();
						checkedIDs.add(id);
					}

				}

				Log.d(Application.APPTAG, "checkedIDs tasks:" + checkedIDs);
				Intent i = new Intent(TaskExpandableListActivity.this,
						SignUp.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("tasks", checkedIDs);
				i.putExtras(bundle);
				startActivity(i);
			}
		});

		Log.d(Application.APPTAG, "viewprofile:" + viewProfile);
		// Set ExpandableListView values
		getExpandableListView().setGroupIndicator(null);

		// Adding ArrayList data to ExpandableListView values
		new RemoteDataTask().execute();
	}

	@Override
	public void onBackPressed() {

		Toast.makeText(this, "Press OK to save your tasks.", Toast.LENGTH_LONG)
				.show();
		return;
	}

	@SuppressWarnings("unchecked")
	private void viewProfile() {
		ParseUser user = ParseUser.getCurrentUser();
		if (user != null && user.get("taskList") != null) {
			for (int i = 0; i < parents.size(); i++) {
				TaskParent<TaskChild> parent = (TaskParent<TaskChild>) parents
						.get(i);

				List<String> userTask = (List<String>) user.get("taskList");
				for (int j = 0; j < userTask.size(); j++) {
					if (ob.get(i).getObjectId().equals(userTask.get(j))) {
						parent.setChecked(true);
					}
				}
			}
		}
	}

	private void loadHosts(final ArrayList<TaskParent<?>> newParents) {
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
			final TaskParent<?> parent = parents.get(groupPosition);

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

			CheckBox checkbox = (CheckBox) convertView
					.findViewById(R.id.checkbox);
			checkbox.setChecked(parent.isChecked());
			checkbox.setOnCheckedChangeListener(new CheckUpdateListener(parent));

			return convertView;
		}

		// This Function used to inflate child rows view
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parentView) {
			final TaskParent<?> parent = parents.get(groupPosition);
			final TaskChild child = (TaskChild) parent.getChildren().get(
					childPosition);

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
			return parents.get(groupPosition).getChildren().get(childPosition);
		}

		// Call when child row clicked
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			/****** When Child row clicked then this function call *******/

			// Log.i(Application.APPTAG, "parent == " + groupPosition
			// + "=  child : ==" + childPosition);
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
			// Log.i(Application.APPTAG, groupPosition + "=  getGroup ");

			return parents.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return parents.size();
		}

		// Call when parent row clicked
		@Override
		public long getGroupId(int groupPosition) {
			// Log.i(Application.APPTAG, groupPosition + "=  getGroupId "
			// + ParentClickStatus);

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
			private final TaskParent<?> parent;

			private CheckUpdateListener(TaskParent<?> parent) {
				this.parent = parent;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// Log.i(Application.APPTAG, "isChecked: " + isChecked);
				parent.setChecked(isChecked);

				((MyExpandableListAdapter) getExpandableListAdapter())
						.notifyDataSetChanged();

			}
		}
		/***********************************************************************/
	}

	// RemoteDataTask AsyncTask
	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(
					TaskExpandableListActivity.this);
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Locate the class table named "Interest" in Parse.com
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Task");
			query.orderByAscending("Task");
			try {
				ob = query.find();
			} catch (ParseException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Void result) {

			// Creating ArrayList of type parent class to store parent class
			// objects
			ArrayList<TaskParent<?>> list = new ArrayList<TaskParent<?>>();

			// Retrieve object "interestName" from Parse.com database
			for (ParseObject interest : ob) {
				final TaskParent<TaskChild> parent = new TaskParent<TaskChild>();
				parent.setText(interest.getString("Task"));

				ArrayList<String> childList = (ArrayList<String>) interest
						.get("child");

				if (childList.size() != 0) {
					parent.setChildren(new ArrayList<TaskChild>());
					for (int i = 0; i < childList.size(); i++) {

						TaskChild child = new TaskChild();
						child.setText(childList.get(i));
						parent.getChildren().add(child);
					}
				}

				list.add(parent);
			}

			loadHosts(list);
			viewProfile();
			// Close the progressdialog
			mProgressDialog.dismiss();
		}
	}
}
