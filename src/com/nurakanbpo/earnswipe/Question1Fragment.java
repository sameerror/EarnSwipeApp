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
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nurakanbpo.earnswipe.helper.CustomViewPager;
import com.nurakanbpo.earnswipe.helper.SimplePagerAdapter;
import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class Question1Fragment extends Fragment {

	private SomeImportantMethods importantMethods;
	LayoutInflater inflater;
	String qId, cId, text, imageURLleft, imageURLright;
	SharedPreferences sharedPreferences;
	List<NameValuePair> listNameValuePairs;
	View view;
	View leftAnswerImage;
	View rightAnswerImage;
	ImageView slideLeft, slideRight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		// reset back press count
		MainActivity.backpressCount = 0;
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_question_type1, container,
				false);
		importantMethods = new SomeImportantMethods(getActivity());
		Log.i("questionTYpe 2", getArguments().toString());
		qId = getArguments().getString("id");
		cId = getArguments().getString("cId");
		text = getArguments().getString("text");
		imageURLleft = getArguments().getString("imageURLleft");
		imageURLright = getArguments().getString("imageURLright");
		listNameValuePairs = new ArrayList<NameValuePair>();
		listNameValuePairs.add(new BasicNameValuePair("token",
				sharedPreferences.getString("token", "")));
		listNameValuePairs.add(new BasicNameValuePair("qId", qId));
		listNameValuePairs.add(new BasicNameValuePair("cId", cId));
		listNameValuePairs.add(new BasicNameValuePair("qType", 2 + ""));
		this.inflater = inflater;
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null) {
			TextView questionText = (TextView) view
					.findViewById(R.id.questionsTextView);
			questionText.setText(getArguments().get("text").toString());
			slideLeft = (ImageView) view.findViewById(R.id.slideLeft);
			slideRight = (ImageView) view.findViewById(R.id.slideRight);
			importantMethods.startImageAnimation(slideLeft);
			importantMethods.startImageAnimation(slideRight);
			// for left sided image
			CustomViewPager leftImageViewPager = (CustomViewPager) view
					.findViewById(R.id.leftImagePager);
			leftAnswerImage = inflater
					.inflate(R.layout.simple_image_view, null);
			leftAnswerImage.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_light));
			View leftSelectedAnswerImage = inflater.inflate(
					R.layout.simple_image_view2, null);
			((ImageView) leftSelectedAnswerImage)
					.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_action_accept));
			((ImageView) leftSelectedAnswerImage)
					.setScaleType(ScaleType.CENTER);
			leftSelectedAnswerImage.setBackgroundColor(getResources().getColor(
					android.R.color.holo_green_light));
			ArrayList<View> leftImagesArray = new ArrayList<View>();
			leftImagesArray.add(0, leftAnswerImage);
			leftImagesArray.add(1, leftSelectedAnswerImage);
			leftImageViewPager.setAdapter(new SimplePagerAdapter(
					leftImagesArray));

			new DownloadQuestionImage().execute(imageURLleft, 0);
			// leftImageViewPager.setPageTransformer(true,
			// new ZoomOutPageTransformer());

			// for right sided image
			CustomViewPager rightImageViewPager = (CustomViewPager) view
					.findViewById(R.id.rightImagePager);
			rightAnswerImage = inflater.inflate(R.layout.simple_image_view,
					null);
			rightAnswerImage.setBackgroundColor(getResources().getColor(
					android.R.color.holo_red_light));
			View rightSelectedAnswerImage = inflater.inflate(
					R.layout.simple_image_view2, null);
			((ImageView) rightSelectedAnswerImage)
					.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_action_accept));
			((ImageView) rightSelectedAnswerImage)
					.setScaleType(ScaleType.CENTER);
			rightSelectedAnswerImage.setBackgroundColor(getResources()
					.getColor(android.R.color.holo_green_light));
			ArrayList<View> rightImagesArray = new ArrayList<View>();
			rightImagesArray.add(0, rightSelectedAnswerImage);
			rightImagesArray.add(1, rightAnswerImage);
			rightImageViewPager.setAdapter(new SimplePagerAdapter(
					rightImagesArray));
			rightImageViewPager.setCurrentItem(1);
			new DownloadQuestionImage().execute(imageURLright, 1);
			// rightImageViewPager.setPageTransformer(true,
			// new ZoomOutPageTransformer());

			setPageChangeListener(leftImageViewPager, new SplashFragment(),
					listNameValuePairs, 0);
			setPageChangeListener(rightImageViewPager, new SplashFragment(),
					listNameValuePairs, 1);
			importantMethods.disableEnableViewPagerScroll(leftImageViewPager);
			importantMethods.disableEnableViewPagerScroll(rightImageViewPager);
			importantMethods.displayWaitMessage((ViewGroup) view, "Swipe in");

		} else {
			Fragment savedFragment = getFragmentManager().getFragment(
					savedInstanceState, "saveFragment");
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_placeholder, savedFragment)
					.commitAllowingStateLoss();
		}
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

	private class PostUserAnswerAsync extends
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
		Log.i("dasfadsf", builder.toString());

		JSONTokener tokener = new JSONTokener(builder.toString());

		jsonObject = new JSONObject(tokener);
		status = httpResponse.getStatusLine().toString();
		if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
			jsonMessage = jsonObject.getString("msg");
			Log.d("answer response", jsonMessage);
			getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_placeholder, new SplashFragment())
					.commitAllowingStateLoss();
		} else {
		}
	}

	private class DownloadQuestionImage extends AsyncTask<Object, Void, Bitmap> {

		String url;
		int position; /* 0=left 1=right */

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Bitmap doInBackground(Object... objects) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof String) {
					url = (String) objects[i];
				} else if (objects[i] instanceof Integer) {
					position = (Integer) objects[i];
				}
			}
			try {
				return importantMethods.getBitmapFromURL(new URL(url));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			switch (position) {
			case 0:
				leftAnswerImage.findViewById(R.id.progressBar1).setVisibility(
						View.GONE);
				((ImageView) leftAnswerImage.findViewById(R.id.answerImage))
						.setImageBitmap(result);

				break;
			case 1:
				rightAnswerImage.findViewById(R.id.progressBar1).setVisibility(
						View.GONE);
				((ImageView) rightAnswerImage.findViewById(R.id.answerImage))
						.setImageBitmap(result);

				break;
			default:
				break;
			}

		}

	}

	private void setPageChangeListener(final ViewPager viewPager,
			final Fragment fragment, final List<NameValuePair> nameValuePairs,
			final int position) {
		/*
		 * imagePosition left = 0 right = 1
		 */
		ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (viewPager.getCurrentItem() == arg0) {
					// importantMethods
					// .disableEnableViewPagerScroll((CustomViewPager)
					// viewPager);
					((CustomViewPager) view.findViewById(R.id.leftImagePager))
							.setPagingEnabled(false);
					((CustomViewPager) view.findViewById(R.id.rightImagePager))
							.setPagingEnabled(false);

					switch (position) {
					case 0:
						nameValuePairs.add(new BasicNameValuePair("answer",
								imageURLleft));
						break;
					case 1:
						nameValuePairs.add(new BasicNameValuePair("answer",
								imageURLright));
						break;
					default:
						break;
					}

					new PostUserAnswerAsync().execute(nameValuePairs);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				importantMethods.startImageAnimation(slideLeft);
				importantMethods.startImageAnimation(slideRight);
			}
		};
		viewPager.setOnPageChangeListener(onPageChangeListener);
	}

}
