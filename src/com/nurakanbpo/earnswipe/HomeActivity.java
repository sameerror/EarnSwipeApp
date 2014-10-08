package com.nurakanbpo.earnswipe;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends FragmentActivity {
	SocialAuthAdapter adapter;
	Button fb_Button;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.home_activity_layout);
		fb_Button = (Button) findViewById(R.id.fbButton);

		adapter = new SocialAuthAdapter(new ResponseListener());

		fb_Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (fb_Button.getText().toString().equalsIgnoreCase("sign in"))
					adapter.authorize(HomeActivity.this, Provider.FACEBOOK);
				else {
					Boolean signOut = adapter.signOut(HomeActivity.this,
							Provider.FACEBOOK.name());
					if (signOut)
						fb_Button.setText("Sign in");

				}
			}
		});

	}

	private final class ResponseListener implements DialogListener {
		public void onComplete(Bundle values) {
			fb_Button.setText("Sign out");
			adapter.getUserProfileAsync(new ProfileDataListener());
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(intent);
		}

		@Override
		public void onBack() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(SocialAuthError arg0) {
			Log.i("Response Listener", arg0.toString());

		}
	}

	// To receive the profile response after authentication
	private final class ProfileDataListener implements
			SocialAuthListener<Profile> {

		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onExecute(String arg0, Profile t) {
			Log.d("Custom-UI", arg0);
			Profile profileMap = t;
			Log.d("Custom-UI", "Validate ID = " + profileMap.getValidatedId());
			Log.d("Custom-UI", "First Name = " + profileMap.getFirstName());
			Log.d("Custom-UI", "Last Name = " + profileMap.getLastName());
			Log.d("Custom-UI", "Email = " + profileMap.getEmail());
			Log.d("Custom-UI", "Gender = " + profileMap.getGender());
			Log.d("Custom-UI", "Country = " + profileMap.getCountry());
			Log.d("Custom-UI", "Language = " + profileMap.getLanguage());
			Log.d("Custom-UI", "Location = " + profileMap.getLocation());
			Log.d("Custom-UI",
					"Profile Image URL = " + profileMap.getProfileImageURL());
		}

	}
}
