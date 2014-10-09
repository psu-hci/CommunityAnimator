package com.example.communityanimator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		final Button menu = (Button) findViewById(R.id.menu);
		final RelativeLayout menuView = (RelativeLayout) findViewById(R.id.menuView);

		menu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (menuView.getVisibility() == View.VISIBLE) {
					menuView.setVisibility(View.GONE);
					menu.setBackgroundResource(R.drawable.ic_menu_off);

				} else {
					menuView.setVisibility(View.VISIBLE);
					menu.setBackgroundResource(R.drawable.ic_menu_on);
				}

			}
		});

		TextView profileItem = (TextView) findViewById(R.id.profileItem);
		profileItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		TextView viewItem = (TextView) findViewById(R.id.viewItem);
		viewItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String[] MenuItems = { "List View", "Map View" };
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater inflater = getLayoutInflater();
				View convertView = inflater.inflate(R.layout.view_dialog, null);
				alertDialog.setView(convertView);
				alertDialog.setTitle("View");
				ListView lv = (ListView) convertView
						.findViewById(R.id.listView1);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						MainActivity.this,
						android.R.layout.simple_list_item_single_choice,
						MenuItems);
				lv.setAdapter(adapter);

				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				lv.setItemChecked(0, true);

				alertDialog.show();

				Button btnSave = (Button) convertView
						.findViewById(R.id.listviewBtn);
				btnSave.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Toast.makeText(MainActivity.this, "Okkk!!",
								Toast.LENGTH_SHORT).show();

					}
				});
			}
		});

		LinearLayout statusItem = (LinearLayout) findViewById(R.id.statusItem);
		statusItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		LinearLayout distanceItem = (LinearLayout) findViewById(R.id.distanceItem);
		distanceItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		TextView locateItem = (TextView) findViewById(R.id.locateItem);
		locateItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		TextView settingsItem = (TextView) findViewById(R.id.settingsItem);
		settingsItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
	}
}
