package com.nurakanbpo.earnswipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyRewardsFragment extends Fragment {

	private View view;
	private String url = "http://www.ktminfosys.com/api/credits/?token=";
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_my_rewards, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (sharedPreferences.contains("credits"))
			((TextView) view.findViewById(R.id.creditsTextView))
					.setText(sharedPreferences.getString("credits", "0"));
		if (sharedPreferences.contains("token")) {
			String token = sharedPreferences.getString("token", "");
			new GetUserCreditsAsync().execute(url + token);
			Log.i("credits", token);
		}
	}

	private class GetUserCreditsAsync extends AsyncTask<String, Void, String> {

		BufferedReader reader = null;
		JSONObject jsonObject;
		String jsonMessage = null;

		@Override
		protected String doInBackground(String... url) {
			HttpResponse httpResponse = sendGETRequest(url[0]);
			if (httpResponse.getStatusLine().toString()
					.equalsIgnoreCase("HTTP/1.1 200 OK")) {

				try {
					reader = new BufferedReader(new InputStreamReader(
							httpResponse.getEntity().getContent(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null;) {
						builder.append(line).append("\n");
					}
					Log.i("myrewards", builder.toString());

					JSONTokener tokener = new JSONTokener(builder.toString());

					jsonObject = new JSONObject(tokener);

					jsonMessage = jsonObject.getString("credits");
					Log.i("credits", jsonMessage);
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

			} else {
				Log.i("status", httpResponse.getStatusLine().toString());
			}
			return jsonMessage;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			((TextView) view.findViewById(R.id.creditsTextView))
					.setText(result);
			sharedPreferences.edit().putString("credits", result).commit();
		}
	}

	private HttpResponse sendGETRequest(String url) {
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);

			request.addHeader("User-Agent", "Mozilla");

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

}
