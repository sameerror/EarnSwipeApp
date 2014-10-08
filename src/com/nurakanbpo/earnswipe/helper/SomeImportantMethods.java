package com.nurakanbpo.earnswipe.helper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.nurakanbpo.earnswipe.MainActivity;
import com.nurakanbpo.earnswipe.R;
import com.nurakanbpo.earnswipe.SplashFragment;

public class SomeImportantMethods {
	Context context;
	Bitmap userImageBitmap;
	FileOutputStream out = null;

	public SomeImportantMethods(Context context) {
		this.context = context;
	}

	public void setPageChangeListener(final ViewPager viewPager,
			final Fragment fragment) {
		/*
		 * imagePosition left = 0 top = 1 right = 2 bottom = 3
		 */
		ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (viewPager.getCurrentItem() == arg0) {

					completeAnimAndExecute(fragment, 600);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		};
		viewPager.setOnPageChangeListener(onPageChangeListener);
	}

	public void completeAnimAndExecute(final Fragment fragment,
			final int timeInMillis) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(timeInMillis);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((FragmentActivity) context)
										.getSupportFragmentManager()
										.beginTransaction()
										.setTransition(
												FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.replace(R.id.fragment_placeholder,
												fragment).commit();
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();
	}

	public void disableEnableViewPagerScroll(final CustomViewPager viewPager) {
		viewPager.setPagingEnabled(false);
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(5000);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewPager.setPagingEnabled(true);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();
	}

	public void disableViewForSomeTime(final ViewGroup viewGroup) {
		enableDisableViewGroup(viewGroup, false);
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(5000);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								enableDisableViewGroup(viewGroup, true);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();
	}

	/**
	 * Enables/Disables all child views in a view group.
	 * 
	 * @param viewGroup
	 *            the view group
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable the
	 *            views.
	 */
	public static void enableDisableViewGroup(ViewGroup viewGroup,
			boolean enabled) {
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = viewGroup.getChildAt(i);
			if (view instanceof ViewGroup) {
				enableDisableViewGroup((ViewGroup) view, enabled);
			} else if (view instanceof CustomViewPager) {
				((CustomViewPager) view).setPagingEnabled(enabled);
			}
			view.setEnabled(enabled);

		}
	}

	public void displayWaitMessage(final ViewGroup viewGroup,
			final String message) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater
				.inflate(R.layout.custom_message_layout, null);

		viewGroup.addView(view);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
				RelativeLayout.TRUE);
		view.setLayoutParams(layoutParams);
		viewGroup.bringChildToFront(view);
		new CountDownTimer(5000, 1000) {

			public void onTick(long millisUntilFinished) {
				((TextView) view.findViewById(R.id.waitMessage))
						.setText(message + " " + millisUntilFinished / 1000
								+ " seconds");
			}

			public void onFinish() {
				viewGroup.removeView(view);
			}
		}.start();

	}

	public void showSplash(final Fragment currentFragment) {
		final FragmentManager fragmentManager = ((FragmentActivity) context)
				.getSupportFragmentManager();
		final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		final Fragment splashFragment = new SplashFragment();
		fragmentTransaction.add(splashFragment, "splash").show(splashFragment)
				.hide(currentFragment).commit();
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(3000);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								fragmentManager.beginTransaction()
										.show(currentFragment)
										.remove(splashFragment).commit();
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();

	}

	public void storeUserImage(GraphUser user, Session session)
			throws IOException, InterruptedException, ExecutionException {

		String path = Environment.getExternalStorageDirectory().toString();

		new File(path + "/EarnSwipe/images/").mkdirs();
		File filename = new File(path + "/EarnSwipe/images/" + user.getId()
				+ ".jpg");
		if (!filename.exists()) {
			filename.createNewFile();
		}

		try {
			out = new FileOutputStream(filename, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String image_url = "http://graph.facebook.com/" + user.getId()
				+ "/picture?type=square";
		DownloadUserImageAsync userImageAsync = new DownloadUserImageAsync();
		userImageAsync.execute(new URL(image_url));
		// userImageBitmap = userImageAsync.get();

	}

	public class DownloadUserImageAsync extends AsyncTask<URL, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(URL... url) {
			return getBitmapFromURL(url[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			userImageBitmap = result;
			userImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Bitmap getBitmapFromURL(URL url) {
		/*--- this method downloads an Image from the given URL, 
		 *  then decodes and returns a Bitmap object
		 ---*/
		try {

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			Log.i("Downloading image", url.getPath());

			InputStream input = connection.getInputStream();

			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					input);
			ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(512 * 1024);
			byte[] buffer = new byte[512 * 1024];
			int read;
			do {
				read = bufferedInputStream.read(buffer, 0, buffer.length);
				if (read > 0)
					byteArrayBuffer.append(buffer, 0, read);
			} while (read >= 0);
			Bitmap myBitmap = null;
			buffer = byteArrayBuffer.toByteArray();
			myBitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
			// Bitmap myBitmap = BitmapFactory.decodeStream(input);
			Log.i("DownloadComplete", url.getPath());
			return myBitmap;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("getBmpFromUrl error: ", e.getMessage().toString());
			return null;
		}
	}

	public HashMap<String, String> convertHeadersToHashMap(Header[] headers) {
		HashMap<String, String> result = new HashMap<String, String>(
				headers.length);
		for (Header header : headers) {
			result.put(header.getName(), header.getValue());
		}
		return result;
	}

	public void startImageAnimation(final ImageView imageView) {
		imageView.setVisibility(View.VISIBLE);
		final AnimationDrawable frameAnimation = (AnimationDrawable) imageView
				.getDrawable();
		frameAnimation.start();
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(1000);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageView.clearAnimation();
								frameAnimation.stop();
								imageView.setVisibility(View.INVISIBLE);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();

	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}

	public void runRingAnimation() {

		int px = dpToPx(50);
		// create imageView
		final ImageView ringImage = new ImageView(context);
		ringImage.setImageResource(R.drawable.ring);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				px, px);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		// get the view to add the image to
		final View rootView = ((Activity) context).getWindow().getDecorView()
				.findViewById(R.id.testLayout);
		// add image to the view
		((ViewManager) rootView).addView(ringImage, layoutParams);

		// animate the image
		AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(context,
				R.animator.test);
		set.setTarget(ringImage);
		set.start();

		// wait 4100ms and then remove the image and animation
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				synchronized (this) {
					try {
						wait(4100);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ringImage.clearAnimation();
								((ViewManager) rootView).removeView(ringImage);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};// thread
		thread.start();
	}

	// public class GetRequestAsync extends AsyncTask<String, Void,
	// HttpResponse> {
	//
	// @Override
	// protected HttpResponse doInBackground(String... url) {
	// return sendGETRequest(url[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(HttpResponse result) {
	// super.onPostExecute(result);
	// }
	// }

	// public String getHeaderResponse(URL url) throws ParseException,
	// IOException {
	// // HttpResponse httpResponse2 = httpResponse;
	// // HttpEntity httpEntity = httpResponse2.getEntity();
	// // Log.i("httpresponse", EntityUtils.toString(httpEntity));
	// URLConnection conn = url.openConnection();
	// Log.w("", conn.getHeaderFields().toString());
	// // Log.i("headers",
	// // convertHeadersToHashMap(httpResponse.getAllHeaders())
	// // .toString());
	// String status = conn.getHeaderField(null);
	// Log.i("status", status);
	// return status;
	// }
	// public class PostUserAnswerAsync extends
	// AsyncTask<List<NameValuePair>, Void, HttpResponse> {
	// String token, qId, answerImage, qType, cId;
	//
	// @Override
	// protected HttpResponse doInBackground(List<NameValuePair>... arg0) {
	// for (int i = 0; i < arg0[0].size(); i++) {
	// if (arg0[0].get(i).getName().contentEquals("token"))
	// token = arg0[0].get(i).getValue();
	// else if (arg0[0].get(i).getName().contentEquals("qId"))
	// qId = arg0[0].get(i).getValue();
	// else if (arg0[0].get(i).getName().contentEquals("answerImage"))
	// answerImage = arg0[0].get(i).getValue();
	// else if (arg0[0].get(i).getName().contentEquals("qType"))
	// qType = arg0[0].get(i).getValue();
	// else if (arg0[0].get(i).getName().contentEquals("cId"))
	// cId = arg0[0].get(i).getValue();
	//
	// }
	// return postUserAnswerData(token, qId, answerImage, qType, cId);
	// }
	//
	// @Override
	// protected void onPostExecute(HttpResponse result) {
	// super.onPostExecute(result);
	// new ParseJSONforAnswerCreditAsync().execute(result);
	// }
	// }
	//
	// private class ParseJSONforAnswerCreditAsync extends
	// AsyncTask<HttpResponse, Void, Void> {
	//
	// @Override
	// protected Void doInBackground(HttpResponse... arg0) {
	// try {
	// parseJSONforAnswerCredit(arg0[0]);
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalStateException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }
	// }
	//
	// private HttpResponse postUserAnswerData(String token, String qId,
	// String answerImage, String qType, String cId) {
	// // Create a new HttpClient and Post Header
	// HttpResponse response = null;
	// HttpClient httpclient = new DefaultHttpClient();
	// HttpPost httppost = new HttpPost(
	// "http://www.ktminfosys.com/api/answer/?token=" + token);
	//
	// try {
	// // Add your data
	// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	// nameValuePairs.add(new BasicNameValuePair("qId", qId));
	// nameValuePairs.add(new BasicNameValuePair("answer", answerImage));
	// nameValuePairs.add(new BasicNameValuePair("type", qType));
	// nameValuePairs.add(new BasicNameValuePair("cId", cId));
	// httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	//
	// // Execute HTTP Post Request
	// response = httpclient.execute(httppost);
	//
	// } catch (ClientProtocolException e) {
	// // TODO Auto-generated catch block
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// }
	// return response;
	// }
	//
	// private void parseJSONforAnswerCredit(HttpResponse httpResponse)
	// throws UnsupportedEncodingException, IllegalStateException,
	// IOException, JSONException {
	// BufferedReader reader = null;
	// JSONObject jsonObject;
	// String jsonMessage = null;
	// String status = null;
	// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	//
	// reader = new BufferedReader(new InputStreamReader(httpResponse
	// .getEntity().getContent(), "UTF-8"));
	// StringBuilder builder = new StringBuilder();
	// for (String line = null; (line = reader.readLine()) != null;) {
	// builder.append(line).append("\n");
	// }
	// JSONTokener tokener = new JSONTokener(builder.toString());
	//
	// jsonObject = new JSONObject(tokener);
	// status = httpResponse.getStatusLine().toString();
	// if (status.equalsIgnoreCase("HTTP/1.1 200 OK")) {
	// jsonMessage = jsonObject.getString("msg");
	// Log.d("answer response", jsonMessage);
	// ((MainActivity) context).getSupportFragmentManager()
	// .beginTransaction()
	// .replace(R.id.fragment_placeholder, new SplashFragment())
	// .commit();
	// } else {
	// }
	// }
}
