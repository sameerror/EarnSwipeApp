package com.nurakanbpo.earnswipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class AttributeFragment extends Fragment implements OnClickListener {

	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private UiLifecycleHelper uiHelper;
	private static final int REAUTH_ACTIVITY_CODE = 100;
	private Spinner educationSpinner;
	private Spinner incomeSpinner;
	private RadioButton maleRadio, femaleRadio;
	private String userGender;
	
	// private ChipsMultiAutoCompleteTextview aboutUserTextview;
	private MultiAutoCompleteTextView aboutUserTextView;
	private DatePicker dobPicker;
	View view;
	String userID;
	SharedPreferences sharedPreferences;
	private Button saveButton;
	private int userEduPosition;
	private int userIncPosition;
	SomeImportantMethods importantMethods;
	private ProgressBar mProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// reset back press count
		MainActivity.backpressCount = 0;
		setRetainInstance(true);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		importantMethods = new SomeImportantMethods(getActivity());
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);

		view = inflater.inflate(R.layout.fragment_attributes, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null) {

			// Find the user's profile picture custom view
			profilePictureView = (ProfilePictureView) view
					.findViewById(R.id.selection_profile_pic);
			profilePictureView.setCropped(true);

			// Find the user's name view
			userNameView = (TextView) view
					.findViewById(R.id.selection_user_name);
			maleRadio = (RadioButton) view.findViewById(R.id.maleRadio);
			femaleRadio = (RadioButton) view.findViewById(R.id.femaleRadio);
			// aboutUserTextview = (ChipsMultiAutoCompleteTextview) view
			// .findViewById(R.id.aboutMeEditText);
			aboutUserTextView = (MultiAutoCompleteTextView) view
					.findViewById(R.id.aboutMeEditText);
			dobPicker = (DatePicker) view.findViewById(R.id.datePicker1);
			saveButton = (Button) view.findViewById(R.id.saveButton);
			saveButton.setOnClickListener(this);
			mProgress = (ProgressBar) view.findViewById(R.id.attrProgressBar);

			// Check for an open session
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				// Get the user's data
				makeMeRequest(session);

			}

			String[] aboutMeArray = getResources().getStringArray(
					R.array.aboutMeArray);
			// ArrayList<ChipsItem> arrCountry = new ArrayList<ChipsItem>();
			// for (int i = 0; i < aboutMeArray.length; i++) {
			// arrCountry.add(new ChipsItem(aboutMeArray[i],
			// R.drawable.ic_launcher));
			//
			// }
			final String[] COUNTRIES = new String[] { "Belgium", "France",
					"Italy", "Germany", "Spain" };

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_dropdown_item_1line,
					COUNTRIES);

			aboutUserTextView.setAdapter(adapter);
			aboutUserTextView.setThreshold(1);
			aboutUserTextView
					.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

			// ChipsAdapter chipsAdapter = new ChipsAdapter(getActivity(),
			// arrCountry);
			// aboutUserTextview.setAdapter(chipsAdapter);

			incomeSpinner = (Spinner) view.findViewById(R.id.incomeSpinner);
			educationSpinner = (Spinner) view
					.findViewById(R.id.educationQualificationSpinner);

			ArrayAdapter<CharSequence> incomeAdapter = ArrayAdapter
					.createFromResource(getActivity(), R.array.incomeArray,
							android.R.layout.simple_spinner_item);
			ArrayAdapter<CharSequence> educationAdapter = ArrayAdapter
					.createFromResource(getActivity(), R.array.educationArray,
							android.R.layout.simple_spinner_item);
			incomeAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			educationAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			incomeSpinner.setAdapter(incomeAdapter);
			educationSpinner.setAdapter(educationAdapter);
			incomeSpinner.setOnItemSelectedListener(onItemSelectedListener);
			educationSpinner.setOnItemSelectedListener(onItemSelectedListener);

		} else {

			Fragment savedFragment = getFragmentManager().getFragment(
					savedInstanceState, "saveFragment");
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_placeholder, savedFragment).commit();
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.saveButton:
			view.setEnabled(false);
			mProgress.setVisibility(View.VISIBLE);
			// showFragment(QUESTION2, true);
			Editor editor = sharedPreferences.edit();
			editor.putString("userID", userID)
					.putString("userName", userNameView.getText().toString())
					.putString("userGender", userGender)
					.putString("aboutUser",
							aboutUserTextView.getText().toString())
					.putInt("userDOBday", dobPicker.getDayOfMonth())
					.putInt("userDOBmonth", dobPicker.getMonth())
					.putInt("userDOByear", dobPicker.getYear())
					.putInt("userEducation", userEduPosition)
					.putInt("userIncome", userIncPosition).commit();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(9);
			nameValuePairs.add(new BasicNameValuePair("id", userID));
			nameValuePairs.add(new BasicNameValuePair("name", userNameView
					.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("gender", userGender));
			nameValuePairs.add(new BasicNameValuePair("about",
					aboutUserTextView.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("DOBday", dobPicker
					.getDayOfMonth() + ""));
			nameValuePairs.add(new BasicNameValuePair("DOBmonth", dobPicker
					.getMonth() + ""));
			nameValuePairs.add(new BasicNameValuePair("DOByear", dobPicker
					.getYear() + ""));
			nameValuePairs.add(new BasicNameValuePair("education",
					userEduPosition + ""));
			nameValuePairs.add(new BasicNameValuePair("income", userIncPosition
					+ ""));
			new RegisterUserAsync().execute(nameValuePairs);

			// FragmentManager fragmentManager = getFragmentManager();
			// int count = fragmentManager.getBackStackEntryCount();
			// if (count == 0) {
			// FragmentTransaction fragmentTransaction = fragmentManager
			// .beginTransaction().setTransition(
			// FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// fragmentTransaction.replace(R.id.fragment_placeholder,
			// new SplashFragment()).commit();
			// } else
			// fragmentManager.popBackStack();
			break;

		default:
			break;
		}

	}

	private void makeMeRequest(final Session session) {
		// Make an API call to get user data and define a
		// new callback to handle the response.
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// If the response is successful
						if (session == Session.getActiveSession()) {
							if (user != null) {
								// Set the id for the ProfilePictureView
								// view that in turn displays the profile
								// picture.
								profilePictureView.setProfileId(user.getId());

								// Set the Textview's text to the user's
								// name.
								userNameView.setText(user.getName());
								userGender = (String) user
										.getProperty("gender");

								userID = user.getId();
								if (userGender.contentEquals("male")) {
									maleRadio.setChecked(true);
								} else {
									femaleRadio.setChecked(true);
								}
								// restore from saved preferences if available
								if (sharedPreferences.contains("userID"))
									if (userID.contentEquals(sharedPreferences
											.getString("userID", ""))) {
										Log.i("SharedPrefrences",
												"there is data as userID matches");
										userNameView.setText(sharedPreferences
												.getString("userName", ""));
										dobPicker.updateDate(sharedPreferences
												.getInt("userDOByear", 1900),
												sharedPreferences.getInt(
														"userDOBmonth", 1),
												sharedPreferences.getInt(
														"userDOBday", 1));
										educationSpinner.setSelection(sharedPreferences
												.getInt("userEducation", 0));
										incomeSpinner.setSelection(sharedPreferences
												.getInt("userIncome", 0));
										aboutUserTextView
												.setText(sharedPreferences
														.getString("aboutUser",
																""));

									}
							}
						}

						if (response.getError() != null) {
							// Handle errors, will do so later.
						}
					}
				});
		request.executeAsync();
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			// Get the user's data.
			makeMeRequest(session);
		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			uiHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		// super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);

		// Save the fragment's instance
		getFragmentManager().putFragment(
				bundle,
				"saveFragment",
				getFragmentManager()
						.findFragmentById(R.id.fragment_placeholder));
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// An item was selected. You can retrieve the selected item using
			// Log.i("Spinner selected item", parent.getItemAtPosition(position)
			// + "");
			switch (parent.getId()) {
			case R.id.educationQualificationSpinner:
				userEduPosition = position;
				Log.i("eduSpinner selected item",
						parent.getItemAtPosition(position) + "");
				break;
			case R.id.incomeSpinner:
				userIncPosition = position;
				Log.i("IncSpinner selected item",
						parent.getItemAtPosition(position) + "");
				break;
			default:
				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

			switch (parent.getId()) {
			case R.id.educationQualificationSpinner:
				userEduPosition = 0;
				Log.i("eduSpinner selected item", parent.getItemAtPosition(0)
						+ "");
				break;
			case R.id.incomeSpinner:
				userIncPosition = 0;
				Log.i("IncSpinner selected item", parent.getItemAtPosition(0)
						+ "");
				break;
			default:
				break;
			}

		}
	};

	public class RegisterUserAsync extends
			AsyncTask<List<NameValuePair>, Void, HttpResponse> {

		@Override
		protected HttpResponse doInBackground(
				List<NameValuePair>... nameValuePairs) {

			return postUserData(nameValuePairs[0]);
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			new ParseJSONuserAsync().execute(result);
		}
	}

	public HttpResponse postUserData(List<NameValuePair> nameValuePairs) {
		// Create a new HttpClient and Post Header
		HttpResponse response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.ktminfosys.com/api/register/?type=app");

		try {
			// Add your data
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return response;
	}

	private class ParseJSONuserAsync extends
			AsyncTask<HttpResponse, Void, Void> {

		@Override
		protected Void doInBackground(HttpResponse... arg0) {
			parseJSONuser(arg0[0]);
			return null;
		}
	}

	private void parseJSONuser(HttpResponse httpResponse) {
		BufferedReader reader = null;
		JSONObject jsonObject;
		String jsonMessage = null;
		String status = httpResponse.getStatusLine().toString();
		Log.i("status Attribute", status);
		try {
			reader = new BufferedReader(new InputStreamReader(httpResponse
					.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

			JSONTokener tokener = new JSONTokener(builder.toString());

			jsonObject = new JSONObject(tokener);

			if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
				jsonMessage = jsonObject.getString("token");
			} else
				jsonMessage = jsonObject.getString("msg");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!jsonMessage.isEmpty()
				&& status.equalsIgnoreCase("HTTP/1.1 200 OK")) {

			Editor editor = sharedPreferences.edit();
			editor.putString("token", jsonMessage).commit();

			if (getFragmentManager().getBackStackEntryCount() == 0) {
				Log.i("attributes fragment", "back stack empty");
				getFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(R.id.fragment_placeholder,
								new SplashFragment()).commit();
			} else {
				getFragmentManager().popBackStack();

				Log.i("attributes fragment", "poping back stack");
			}
		} else {
			// getFragmentManager()
			// .beginTransaction()
			// .replace(R.id.fragment_placeholder, new AttributeFragment())
			// .commit();
		}
	}
}
