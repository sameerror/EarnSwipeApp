package com.nurakanbpo.earnswipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nurakanbpo.earnswipe.helper.MagicTextView;
import com.nurakanbpo.earnswipe.helper.SimpleConnectionCheck;
import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class ValidateUserFragment extends Fragment {

	SomeImportantMethods importantMethods;
	View view;
	String userId;
	private static final int REAUTH_ACTIVITY_CODE = 100;
	HttpResponse getResponse;
	MagicTextView message;
	SharedPreferences sharedPreferences;
	GetRequestAsync getRequestAsync = new GetRequestAsync();
	PostRequestAsync postRequestAsync = new PostRequestAsync();
	ParseJsonAsync parseJsonAsync = new ParseJsonAsync();

	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
		importantMethods = new SomeImportantMethods(getActivity());
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		setRetainInstance(true);
		this.context = getActivity();
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_splash, container, false);
		message = (MagicTextView) view.findViewById(R.id.splashText);
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i("validate", "onactivity created");
		SimpleConnectionCheck check = new SimpleConnectionCheck(getActivity());
		if (check.isNetworkConnected()) {

			message.setText("Validating user");
		} else {
			view.findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
			message.setText("No Network Connection");
			message.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_action_warning, 0, 0, 0);
		}
		if (savedInstanceState == null) {
			Log.i("validate", "null savedInstance");

		} else {
			Log.i("savedinstance", savedInstanceState + "");
			Fragment savedFragment = getFragmentManager().getFragment(
					savedInstanceState, "saveFragment");
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_placeholder, savedFragment)
					.commitAllowingStateLoss();

		}

	}

	public class GetRequestAsync extends AsyncTask<String, Void, HttpResponse> {
		URL url = null;

		@Override
		protected HttpResponse doInBackground(String... urlStrings) {

			Log.e("validate", "get request");
			try {
				url = new URL(urlStrings[0]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return sendGETRequest(urlStrings[0]);
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);
			Log.d("getRequest header response", result.getStatusLine()
					.toString());
			new ParseJsonAsync().execute(result, "get");
			// parseJsonAsync.execute(result, "get");
		}
	}

	public class PostRequestAsync extends AsyncTask<String, Void, HttpResponse> {
		URL url = null;

		@Override
		protected HttpResponse doInBackground(String... userId) {
			try {

				url = new URL(
						"http://www.ktminfosys.com/api/register/?type=facebook");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return postData(userId[0]);
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);
			Log.d("PostRequest header response", result.getStatusLine()
					.toString());
			message.setText("Registering user");
			// parseJsonAsync = new ParseJsonAsync();
			// parseJsonAsync.execute(result, "post");
			new ParseJsonAsync().execute(result, "post");
		}
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {

		if (session != null && session.isOpened()) {
			// Get the user's data.
			makeMeRequest(session);
			// get user likes
			getUserLikes(session);
		}

	}

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {

			onSessionStateChange(session, state, exception);
		}
	};

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
								userId = user.getId();
								Log.e("validate make me request", userId);
								new GetRequestAsync()
										.execute("http://www.ktminfosys.com/api/login/?user="
												+ userId);
								// getRequestAsync
								// .execute("http://www.ktminfosys.com/api/login/?user="
								// + userId);

							}
						}
						if (response.getError() != null) {
							// Handle errors, will do so later.
						}
					}
				});
		request.executeAsync();
	}

	@Override
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
		getFragmentManager().putFragment(
				bundle,
				"saveFragment",
				getFragmentManager()
						.findFragmentById(R.id.fragment_placeholder));
		if (getRequestAsync.getStatus() == Status.RUNNING)
			getRequestAsync.cancel(true);
		if (postRequestAsync.getStatus() == Status.RUNNING)
			postRequestAsync.cancel(true);
		if (parseJsonAsync.getStatus() == Status.RUNNING)
			parseJsonAsync.cancel(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();

		if (getRequestAsync.getStatus() == Status.RUNNING)
			getRequestAsync.cancel(true);
		if (postRequestAsync.getStatus() == Status.RUNNING)
			postRequestAsync.cancel(true);
		if (parseJsonAsync.getStatus() == Status.RUNNING)
			parseJsonAsync.cancel(true);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private class ParseJsonAsync extends
			AsyncTask<Object, Void, List<NameValuePair>> {
		HttpResponse httpResponse = null;
		String getOrPost = null;

		// URL url;

		@Override
		protected List<NameValuePair> doInBackground(Object... arg0) {
			for (int i = 0; i < arg0.length; i++) {
				if (arg0[i] instanceof HttpResponse) {
					Log.d("parse jason async", "instance of http");
					httpResponse = (HttpResponse) arg0[i];
				} else if (arg0[i] instanceof String) {
					getOrPost = (String) arg0[i];
					Log.d("parse jason async", "instance of string");
				}
				// else if (arg0[i] instanceof URL) {
				// url = (URL) arg0[i];
				// Log.d("parse jason async", "instance of URL");
				// }
			}

			Log.e("validate", "parse json " + getOrPost);
			return parseJSON(httpResponse, getOrPost);
		}

		@Override
		protected void onPostExecute(List<NameValuePair> result) {
			super.onPostExecute(result);
			String status = null;
			String jsonMessage = null;
			Log.i("parse JSON async", "onpostexecute");
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).getName().contentEquals("status"))
					status = result.get(i).getValue();
				else if (result.get(i).getName().contentEquals("token"))
					jsonMessage = result.get(i).getValue();
				else if (result.get(i).getName().contentEquals("msg"))
					jsonMessage = result.get(i).getValue();
			}

			if (getOrPost.contentEquals("get")) {
				Log.i("parse JSON async", "onpostexecute GET " + status);
				if (!jsonMessage.isEmpty() && status.equalsIgnoreCase("200 OK")) {
					Editor editor = sharedPreferences.edit();
					editor.putString("token", jsonMessage).commit();

					((FragmentActivity) context)
							.getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_placeholder,
									new SplashFragment())
							.commitAllowingStateLoss();

				} else {
					postRequestAsync.execute(userId);
					message.setText("Registering user");
				}
			} else {
				Log.i("parse JSON async", "onpostexecute POST");
				if (!jsonMessage.isEmpty()
						&& jsonMessage
								.equalsIgnoreCase("user registered successfully.")) {

					((FragmentActivity) context)
							.getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_placeholder,
									new AttributeFragment())
							.commitAllowingStateLoss();

				} else {
					message.setText(jsonMessage);
					Log.i("parse JSON async", "onpostexecute POST "
							+ jsonMessage);
				}
			}

		}
	}

	private List<NameValuePair> parseJSON(HttpResponse httpResponse,
			String getOrPost) {
		BufferedReader reader = null;
		JSONObject jsonObject;
		String jsonMessage = null;
		String status = null;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		try {
			reader = new BufferedReader(new InputStreamReader(httpResponse
					.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			JSONTokener tokener = new JSONTokener(builder.toString());

			jsonObject = new JSONObject(tokener);

			status = httpResponse.getStatusLine().toString();

			if (getOrPost.equalsIgnoreCase("get")) {
				if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
					jsonMessage = jsonObject.getString("token");
					nameValuePairs.add(new BasicNameValuePair("status",
							"200 OK"));
					nameValuePairs.add(new BasicNameValuePair("token",
							jsonMessage));
				} else {
					jsonMessage = jsonObject.getString("msg");
					nameValuePairs
							.add(new BasicNameValuePair("status", status));
					nameValuePairs.add(new BasicNameValuePair("msg",
							jsonMessage));
				}
			} else {
				jsonMessage = jsonObject.getString("msg");
				nameValuePairs.add(new BasicNameValuePair("status", status));
				nameValuePairs.add(new BasicNameValuePair("msg", jsonMessage));

			}

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
		Log.e("validate", "parse json " + nameValuePairs.toString());
		return nameValuePairs;
	}

	private HttpResponse postData(String userId) {
		// Create a new HttpClient and Post Header
		HttpResponse response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.ktminfosys.com/api/register/?type=facebook");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("id", userId));

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

	private HttpResponse sendGETRequest(String url) {
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);

			request.addHeader("User-Agent", "Mozilla");
			// try {
			// request.setURI(new URI(url));
			// } catch (URISyntaxException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			response = client.execute(request);

			return response;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;

	}

	private void getUserLikes(Session session) {

		Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
				this, Arrays.asList("user_likes"));
		session.requestNewReadPermissions(newPermissionsRequest);

		/* make the API call */
		new Request(session, "/me/likes", null, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {

						Log.i("Facebook user likes", response.toString());

					}
				}).executeAsync();
	}

}
