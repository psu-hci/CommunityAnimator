package com.example.communityanimator;

import java.util.ArrayList;

import com.example.communityanimator.Categories;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class SignUp extends Activity {
	 
	 MyCustomAdapter dataAdapter = null;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.user_profile);
	 
	  //Generate list View from ArrayList
	  displayListView();
	 
	 // checkButtonClick();
	 
	 }
	 
	 private void displayListView() {
	 
	  //Array list of countries
	  ArrayList<Categories> countryList = new ArrayList<Categories>();
	  Categories country = new Categories("Agriculture and Food",false);
	  countryList.add(country);
	  country = new Categories("Arts and Culture",true);
	  countryList.add(country);
	  country = new Categories("Built Environment",false);
	  countryList.add(country);
	  country = new Categories("Business and Entrepreneurship",true);
	  countryList.add(country);
	  country = new Categories("Civic Engagement",true);
	  countryList.add(country);
	  country = new Categories("Communication",false);
	  countryList.add(country);
	  country = new Categories("Community",false);
	  countryList.add(country);
	  country = new Categories("Economy",false);
	  countryList.add(country);
	  country = new Categories("Education and Learning",false);
	  countryList.add(country);
	  country = new Categories("Energy",false);
	  countryList.add(country);
	  country = new Categories("Environment and Sustainability",false);
	  countryList.add(country);
	  country = new Categories("Health and Wellness",false);
	  countryList.add(country);
	  country = new Categories("Justice and Equality",false);
	  countryList.add(country);
	  country = new Categories("Technology",false);
	  countryList.add(country);
	  country = new Categories("Transportation",false);
	  countryList.add(country);
	  
	 
	  //create an ArrayAdaptar from the String Array
	  dataAdapter = new MyCustomAdapter(this,
	    R.layout.interest_items, countryList);
	  ListView listView = (ListView) findViewById(R.id.listView1);
	  // Assign adapter to ListView
	  listView.setAdapter(dataAdapter);
	 
	 
	  listView.setOnItemClickListener(new OnItemClickListener() {
	   public void onItemClick(AdapterView<?> parent, View view,
	     int position, long id) {
	    // When clicked, show a toast with the TextView text
	    Categories country = (Categories) parent.getItemAtPosition(position);
	    Toast.makeText(getApplicationContext(),
	      "Clicked on Row: " + country.getName(), 
	      Toast.LENGTH_LONG).show();
	   }
	  });
	 
	 }
	 
	 private class MyCustomAdapter extends ArrayAdapter<Categories> {
	 
	  private ArrayList<Categories> countryList;
	 
	  public MyCustomAdapter(Context context, int textViewResourceId, 
	    ArrayList<Categories> countryList) {
	   super(context, textViewResourceId, countryList);
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
	   Log.v("ConvertView", String.valueOf(position));
	 
	   if (convertView == null) {
	   LayoutInflater vi = (LayoutInflater)getSystemService(
	     Context.LAYOUT_INFLATER_SERVICE);
	   convertView = vi.inflate(R.layout.interest_items, null);
	 
	   holder = new ViewHolder();
	   holder.name = (TextView) convertView.findViewById(R.id.code);
	   holder.code = (CheckBox) convertView.findViewById(R.id.checkBox1);
	   convertView.setTag(holder);
	 
	    holder.code.setOnClickListener( new View.OnClickListener() {  
	     public void onClick(View v) {  
	      CheckBox cb = (CheckBox) v ;  
	      Categories country = (Categories) cb.getTag();  
	      Toast.makeText(getApplicationContext(),
	       "Clicked on Checkbox: " + cb.getText() +
	       " is " + cb.isChecked(), 
	       Toast.LENGTH_LONG).show();
	      country.setSelected(cb.isChecked());
	     }  
	    });  
	   } 
	   else {
	    holder = (ViewHolder) convertView.getTag();
	   }
	 
	   Categories country = countryList.get(position);
	   holder.name.setText(country.getName());
	   holder.code.setChecked(country.isSelected());
	 
	   return convertView;
	 
	  }
	 
	 }
	 
//	 private void checkButtonClick() {
//	 
//	 
//	 Button myButton = (Button) findViewById(R.id.findSelected);
//	 myButton.setOnClickListener(new OnClickListener() {
//	 
//	   @Override
//	   public void onClick(View v) {
//	 
//	    StringBuffer responseText = new StringBuffer();
//	    responseText.append("The following were selected...\n");
//	 
//	    ArrayList<Country> countryList = dataAdapter.countryList;
//	    for(int i=0;i<countryList.size();i++){
//	     Country country = countryList.get(i);
//	     if(country.isSelected()){
//	      responseText.append("\n" + country.getName());
//	     }
//	    }
//	 
//	    Toast.makeText(getApplicationContext(),
//	      responseText, Toast.LENGTH_LONG).show();
//	 
//	   }
//	  });
//	 
//	 }
}
	 