package com.nurakanbpo.earnswipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nurakanbpo.earnswipe.helper.MagicTextView;
import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class SplashFragment extends Fragment {
	SomeImportantMethods importantMethods;
	SharedPreferences sharedPreferences;
	MagicTextView message;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater
				.inflate(R.layout.fragment_splash, container, false);
		importantMethods = new SomeImportantMethods(getActivity());
		message = (MagicTextView) view.findViewById(R.id.splashText);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			// Random random = new Random();
			//
			// if (random.nextInt() % 3 == 0)
			// importantMethods.completeAnimAndExecute(
			// new Question3Fragment(), 4000);
			// else if (random.nextInt() % 2 == 0)
			// importantMethods.completeAnimAndExecute(
			// new Question2Fragment(), 4000);
			// else
			// importantMethods.completeAnimAndExecute(
			// new Question1Fragment(), 4000);
			String token = sharedPreferences.getString("token", "");
			message.setText("Fetching survey");
			new AskQuestionAsync().execute(token);
		} else {
			Fragment savedFragment = getFragmentManager().getFragment(
					savedInstanceState, "saveFragment");
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_placeholder, savedFragment)
					.commitAllowingStateLoss();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		getFragmentManager().putFragment(
				outState,
				"saveFragment",
				getFragmentManager()
						.findFragmentById(R.id.fragment_placeholder));
	}

	private class AskQuestionAsync extends
			AsyncTask<String, Void, HttpResponse> {
		URL url = null;

		@Override
		protected HttpResponse doInBackground(String... token) {
			String completeURlString = "http://www.ktminfosys.com/api/question/?token="
					+ token[0];
			try {
				url = new URL(completeURlString);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				return postForQuestion(url);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);
			Log.i("AskQuestion Async", result.getStatusLine().toString());
			new ParseJSONforQuestionAsync().execute(result);
		}

	}

	private HttpResponse postForQuestion(URL url) throws URISyntaxException {
		HttpResponse response = null;

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url.toURI());

		try {
			// Add your data
			// httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return response;

	}

	private class ParseJSONforQuestionAsync extends
			AsyncTask<Object, Void, Void> {
		HttpResponse httpResponse;
		// URL url;
		String temp = null;

		@Override
		protected Void doInBackground(Object... arg0) {
			for (int i = 0; i < arg0.length; i++) {
				if (arg0[i] instanceof HttpResponse)
					httpResponse = (HttpResponse) arg0[i];
				// else if (arg0[i] instanceof URL)
				// url = (URL) arg0[i];
			}
			temp = parseJSONforQuestion(httpResponse);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (temp != null) {
				message.setText(temp);
			}
		}

	}

	private String parseJSONforQuestion(HttpResponse httpResponse) {
		BufferedReader reader = null;
		JSONObject jsonObject;
		String jsonMessage = null;
		String questionId = null, questionText = null, imageTop = null, imageBottom = null, imageLeft = null, imageRight = null, newToken = null;
		String cId = null;
		int questionType = 0, surveyEnd = 1;/* 1 = yes 0 = no */
		String status = null;
		boolean isJSONempty = true;
		JSONObject imageArray;
		status = httpResponse.getStatusLine().toString();
		Log.i("status", status);
		try {
			reader = new BufferedReader(new InputStreamReader(httpResponse
					.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			JSONTokener tokener = new JSONTokener(builder.toString());
			Log.i("splash fragment parseJSONforQ", builder.toString());
			jsonObject = new JSONObject(tokener);

			if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
				questionId = jsonObject.getString("id");
				questionText = jsonObject.getString("text");
				questionType = jsonObject.getInt("type");
				surveyEnd = jsonObject.getInt("end");
				imageArray = jsonObject.getJSONObject("images");
				switch (questionType) {
				case 1:
					imageTop = imageArray.getString("top");
					break;
				case 2:
					imageLeft = imageArray.getString("left");
					imageRight = imageArray.getString("right");
					break;
				case 3:
					imageTop = imageArray.getString("top");
					imageBottom = imageArray.getString("bottom");
					imageLeft = imageArray.getString("left");
					imageRight = imageArray.getString("right");
					break;

				default:
					break;
				}

				newToken = jsonObject.getString("token");
				cId = jsonObject.getString("cid");

				isJSONempty = false;
			} else {
				jsonMessage = jsonObject.getString("msg");
				isJSONempty = false;
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
		if (!isJSONempty) {
			if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {

				Editor editor = sharedPreferences.edit();
				editor.putString("token", newToken).commit();
				if (questionType == 1) {

					Question3Fragment question3Fragment = new Question3Fragment();
					Bundle args = new Bundle();
					args.putString("text", questionText);
					args.putString("imageURL", imageTop);
					args.putString("id", questionId);
					args.putString("cId", cId);
					question3Fragment.setArguments(args);

					getFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_placeholder,
									question3Fragment)
							.commitAllowingStateLoss();
				} else if (questionType == 2) {
					Question1Fragment question1Fragment = new Question1Fragment();
					Bundle args = new Bundle();
					args.putString("text", questionText);
					args.putString("imageURLleft", imageLeft);
					args.putString("imageURLright", imageRight);
					args.putString("id", questionId);
					args.putString("cId", cId);
					question1Fragment.setArguments(args);
					getFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_placeholder,
									question1Fragment)
							.commitAllowingStateLoss();
				} else {
					Question2Fragment question2Fragment = new Question2Fragment();
					Bundle args = new Bundle();
					args.putString("text", questionText);
					args.putString("imageURLleft", imageLeft);
					args.putString("imageURLright", imageRight);
					args.putString("imageURLtop", imageTop);
					args.putString("imageURLbottom", imageBottom);
					args.putString("id", questionId);
					args.putString("cId", cId);
					question2Fragment.setArguments(args);

					getFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_placeholder,
									question2Fragment)
							.commitAllowingStateLoss();

				}
			} else {
				Log.i("Splash fragment", jsonMessage);

			}
		} else {
			Log.i("Splash fragment", "empty json");

		}
		return jsonMessage;

	}
}
