package com.nurakanbpo.earnswipe.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SimpleConnectionCheck {
	Context context;

	public SimpleConnectionCheck(Context context) {
		this.context = context;
	}

	public boolean isNetworkConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

}
