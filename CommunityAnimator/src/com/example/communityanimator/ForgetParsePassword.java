package com.example.communityanimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgetParsePassword extends Activity {
	EditText et_forgetpassword = null;
	Button btn_submitforgetpassword = null;
	String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgetpassword);

		et_forgetpassword = (EditText) findViewById(R.id.et_forgetpassword);
		btn_submitforgetpassword = (Button) findViewById(R.id.btn_submitforgetpassword);

		btn_submitforgetpassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				password = et_forgetpassword.getText().toString();
				checkEmailID();

			}
		});

	}

	protected void checkEmailID() {
		if (TextUtils.isEmpty(password)) {
			et_forgetpassword
					.setError(getString(R.string.error_field_required));
		} else if (!password.contains("@")) {
			et_forgetpassword.setError(getString(R.string.error_invalid_email));
		} else
			forgotPassword(password);
	}

	public void forgotPassword(String email) {

		ParseUser.requestPasswordResetInBackground(email,
				new RequestPasswordResetCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							// success!
							Toast.makeText(
									getApplicationContext(),
									"Successfully sent link to your email for reset Password",
									Toast.LENGTH_LONG).show();

							Intent intent = new Intent(
									ForgetParsePassword.this, Login.class);
							startActivity(intent);
						} else {
							Toast.makeText(
									getApplicationContext(),
									"Failed to sent link to your email for reset Password: "
											+ e.getMessage(), Toast.LENGTH_LONG)
									.show();
						}
					}
				});
	}
}