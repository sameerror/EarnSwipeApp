package com.nurakanbpo.earnswipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nurakanbpo.earnswipe.helper.SimpleConnectionCheck;
import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class Question3Fragment extends Fragment {
	private SomeImportantMethods importantMethods;
	View view;
	ImageView questionImageView;
	String qId, cId, text, imageURL;
	// Bitmap questionImageBitmap;
	SharedPreferences sharedPreferences;
	List<NameValuePair> listNameValuePairs;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		// reset back press count
		MainActivity.backpressCount = 0;
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.fragment_question_type3, container,
				false);

		importantMethods = new SomeImportantMethods(getActivity());
		importantMethods.disableViewForSomeTime((ViewGroup) view);
		importantMethods.displayWaitMessage((ViewGroup) view, "Rate in");
		final RatingBar ratingBar = (RatingBar) view
				.findViewById(R.id.ratingBar1);

		qId = getArguments().getString("id");
		cId = getArguments().getString("cId");
		text = getArguments().getString("text");
		imageURL = getArguments().getString("imageURL");
		listNameValuePairs = new ArrayList<NameValuePair>();
		listNameValuePairs.add(new BasicNameValuePair("token",
				sharedPreferences.getString("token", "")));
		listNameValuePairs.add(new BasicNameValuePair("qId", qId));
		listNameValuePairs.add(new BasicNameValuePair("cId", cId));
		listNameValuePairs.add(new BasicNameValuePair("qType", 1 + ""));
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar arg0, float rating,
					boolean arg2) {
				ratingBar.setEnabled(false);
				listNameValuePairs.add(new BasicNameValuePair("answer",
						ratingBar.getRating() + ""));
				if (new SimpleConnectionCheck(getActivity())
						.isNetworkConnected()) {
					new PostUserAnswerAsync().execute(listNameValuePairs);
				} else {
					ratingBar.setRating(0);
					Toast.makeText(getActivity(), "Failure contacting server",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		questionImageView = (ImageView) view
				.findViewById(R.id.questionImageView);
		TextView questionText = (TextView) view
				.findViewById(R.id.questionsTextView);
		new DownloadQuestionImage().execute(getArguments()
				.getString("imageURL"));
		questionText.setText(getArguments().get("text").toString());

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		// super.onSaveInstanceState(bundle);
		getFragmentManager().putFragment(
				bundle,
				"saveFragment",
				getFragmentManager()
						.findFragmentById(R.id.fragment_placeholder));
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private class DownloadQuestionImage extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... url) {

			try {
				return importantMethods.getBitmapFromURL(new URL(url[0]));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			questionImageView.setImageBitmap(result);
		}

	}

	public class PostUserAnswerAsync extends
			AsyncTask<List<NameValuePair>, Void, HttpResponse> {
		String token, qId, answerImage, qType, cId;

		@Override
		protected HttpResponse doInBackground(List<NameValuePair>... arg0) {
			for (int i = 0; i < arg0[0].size(); i++) {
				if (arg0[0].get(i).getName().contentEquals("token"))
					token = arg0[0].get(i).getValue();
				else if (arg0[0].get(i).getName().contentEquals("qId"))
					qId = arg0[0].get(i).getValue();
				else if (arg0[0].get(i).getName().contentEquals("answer"))
					answerImage = arg0[0].get(i).getValue();
				else if (arg0[0].get(i).getName().contentEquals("qType"))
					qType = arg0[0].get(i).getValue();
				else if (arg0[0].get(i).getName().contentEquals("cId"))
					cId = arg0[0].get(i).getValue();

			}
			return postUserAnswerData(token, qId, answerImage, qType, cId);
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);
			new ParseJSONforAnswerCreditAsync().execute(result);
		}
	}

	private class ParseJSONforAnswerCreditAsync extends
			AsyncTask<HttpResponse, Void, Void> {

		@Override
		protected Void doInBackground(HttpResponse... arg0) {
			try {
				parseJSONforAnswerCredit(arg0[0]);
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
			return null;
		}
	}

	private HttpResponse postUserAnswerData(String token, String qId,
			String answerImage, String qType, String cId) {
		// Create a new HttpClient and Post Header
		HttpResponse response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.ktminfosys.com/api/answer/?token=" + token);

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("qid", qId));
			nameValuePairs.add(new BasicNameValuePair("answer", answerImage));
			nameValuePairs.add(new BasicNameValuePair("type", qType));
			nameValuePairs.add(new BasicNameValuePair("cid", cId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.d("answer", qId + " " + cId + " " + " " + qType + " "
					+ answerImage);
			// Execute HTTP Post Request
			response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return response;
	}

	private void parseJSONforAnswerCredit(HttpResponse httpResponse)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException, JSONException {
		BufferedReader reader = null;
		JSONObject jsonObject;
		String jsonMessage = null;
		String status = null;
		// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		reader = new BufferedReader(new InputStreamReader(httpResponse
				.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line).append("\n");
		}
		Log.i("answer JSON", builder.toString());
		JSONTokener tokener = new JSONTokener(builder.toString());

		jsonObject = new JSONObject(tokener);

		status = httpResponse.getStatusLine().toString();
		if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
			jsonMessage = jsonObject.getString("msg");
			Log.d("answer response", jsonMessage);
			((MainActivity) getActivity()).getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_placeholder, new SplashFragment())
					.commitAllowingStateLoss();
		} else {
		}
	}
}
