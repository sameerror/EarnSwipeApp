package com.nurakanbpo.earnswipe;

import java.util.Arrays;

import com.facebook.widget.LoginButton;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LogInFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		MainActivity.mySlidingMenu
				.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setRetainInstance(true);
		View view = inflater
				.inflate(R.layout.fragment_log_in, container, false);

		// LoginButton authButton = (LoginButton) view
		// .findViewById(R.id.authButton);
		// authButton.setFragment(this);
		// authButton.setReadPermissions(Arrays.asList("user_likes"));

		return view;
	}

}
