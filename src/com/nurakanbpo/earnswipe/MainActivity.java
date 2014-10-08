package com.nurakanbpo.earnswipe;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.UserSettingsFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends FragmentActivity {

	// private Fragment logInFragment = new LogInFragment();
	// private Fragment attributeFragment = new AttributeFragment();
	// private Fragment question1Fragment = new Question1Fragment();
	// private Fragment question2Fragment = new Question2Fragment();
	// private Fragment question3Fragment = new Question3Fragment();

	private boolean isResumed = false;
	private UiLifecycleHelper uiHelper;
	public static int backpressCount = 0;
	private MenuItem settings;
	private MenuItem logOut;
	private Bundle savedInstanceState;
	public static SlidingMenu mySlidingMenu;
	View slidingMenuList;
	public static ListView slidingMenuListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.savedInstanceState = savedInstanceState;

		setContentView(R.layout.home_layout);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState == null) {

			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			transaction.replace(R.id.fragment_placeholder, new LogInFragment());
			transaction.commitAllowingStateLoss();

		} else {

			Fragment savedFragment = getSupportFragmentManager().getFragment(
					savedInstanceState, "saveFragment");
			Log.i("restoring fragment", savedFragment + "");
			getSupportFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.fragment_placeholder, savedFragment)
					.commitAllowingStateLoss();
		}
		// for sliding menu
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		slidingMenuList = inflater.inflate(R.layout.sliding_menu, null);
		List<String> menuList = new ArrayList<String>();
		String[] menuArray = getResources().getStringArray(R.array.menuArray);
		for (int i = 0; i < menuArray.length; i++) {
			menuList.add(i, menuArray[i]);
		}
		ArrayAdapter<String> simpleArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_text_view, R.id.textView1, menuList);

		slidingMenuListView = (ListView) slidingMenuList
				.findViewById(R.id.slidingMenuListView);

		slidingMenuListView.setAdapter(simpleArrayAdapter);

		slidingMenuListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						String menuText = ((TextView) view
								.findViewById(R.id.textView1)).getText()
								.toString();
						if (menuText.equalsIgnoreCase("my rewards")) {
							Class<? extends Object> currentFragmentClass = getSupportFragmentManager()
									.findFragmentById(R.id.fragment_placeholder)
									.getClass();
							// do nothing if already in attributes
							if (currentFragmentClass == MyRewardsFragment.class)
								mySlidingMenu.toggle();
							else {
								getSupportFragmentManager()
										.beginTransaction()
										.setTransition(
												FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.addToBackStack("stack")
										.replace(R.id.fragment_placeholder,
												new MyRewardsFragment())
										.commitAllowingStateLoss();
								getFragmentManager()
										.executePendingTransactions();
								mySlidingMenu.toggle();
							}
						} else if (menuText.equalsIgnoreCase("my profile")) {
							Class<? extends Object> currentFragmentClass = getSupportFragmentManager()
									.findFragmentById(R.id.fragment_placeholder)
									.getClass();
							// do nothing if already in attributes
							if (currentFragmentClass == AttributeFragment.class)
								mySlidingMenu.toggle();
							else {
								getSupportFragmentManager()
										.beginTransaction()
										.setTransition(
												FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.addToBackStack("stack")
										.replace(R.id.fragment_placeholder,
												new AttributeFragment())
										.commitAllowingStateLoss();
								getFragmentManager()
										.executePendingTransactions();
								mySlidingMenu.toggle();
							}
						} else if (menuText.equalsIgnoreCase("log out")) {
							Class<? extends Object> currentFragmentClass = getSupportFragmentManager()
									.findFragmentById(R.id.fragment_placeholder)
									.getClass();
							// do nothing if already in log out
							if (currentFragmentClass == com.facebook.widget.UserSettingsFragment.class)
								mySlidingMenu.toggle();
							else {
								getSupportFragmentManager()
										.beginTransaction()
										.setTransition(
												FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.addToBackStack("stack")
										.replace(R.id.fragment_placeholder,
												new UserSettingsFragment())
										.commitAllowingStateLoss();
								getFragmentManager()
										.executePendingTransactions();
								mySlidingMenu.toggle();
							}
						} else if (menuText.equalsIgnoreCase("about app")) {
							Toast.makeText(MainActivity.this, menuText,
									Toast.LENGTH_SHORT).show();
							mySlidingMenu.toggle();
						}
					}
				});
		mySlidingMenu = new SlidingMenu(this);
		mySlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		mySlidingMenu.setMode(SlidingMenu.LEFT);
		mySlidingMenu.setFadeDegree(0.35f);
		mySlidingMenu.setShadowWidth(10);
		// mySlidingMenu.setShadowDrawable(R.drawable.blurr);
		mySlidingMenu.setBehindOffset(250);
		mySlidingMenu.setMenu(slidingMenuList);
		mySlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
	}

	@Override
	public void onResume() {
		super.onResume();
		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
			Log.i("onresume", "calling session state change");
		}

		uiHelper.onResume();
		isResumed = true;
		Class<? extends Object> currentFragmentClass = getSupportFragmentManager()
				.findFragmentById(R.id.fragment_placeholder).getClass();
		// do not display menu during log in
		if (currentFragmentClass == LogInFragment.class)
			mySlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		isResumed = false;

		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(R.id.fragment_placeholder);
		Log.w("currentFragment", currentFragment + "");
		if (currentFragment.getClass() != LogInFragment.class)
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
				getSupportFragmentManager().putFragment(savedInstanceState,
						"saveFragment", currentFragment);
			}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(R.id.fragment_placeholder);
		Log.w("currentFragment", currentFragment + "");
		getSupportFragmentManager().putFragment(outState, "saveFragment",
				currentFragment);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		// Only make changes if the activity is visible
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			// Get the number of entries in the back stack
			int backStackSize = manager.getBackStackEntryCount();
			// Clear the back stack
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			if (state.isOpened()) {
				// If the session state is open:
				// Show the authenticated fragment
				if (savedInstanceState == null) {
					Log.i("main activity", "on session statechange");
					FragmentTransaction trans = manager.beginTransaction();
					trans.replace(R.id.fragment_placeholder,
							new ValidateUserFragment())
							.commitAllowingStateLoss();

					// session.openForRead(new Session.OpenRequest(this)
					// .setPermissions(Arrays.asList("user_likes"))
					// .setCallback(callback));
				} else {
					Fragment savedFragment = manager.getFragment(
							savedInstanceState, "saveFragment");
					manager.beginTransaction()
							.replace(R.id.fragment_placeholder, savedFragment)
							.commitAllowingStateLoss();
				}
			} else if (state.isClosed()) {
				// If the session state is closed:
				// Show the login fragment
				FragmentTransaction trans = manager.beginTransaction();
				trans.replace(R.id.fragment_placeholder, new LogInFragment())
						.commitAllowingStateLoss();

			}
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();
		FragmentManager manager = getSupportFragmentManager();
		Fragment savedFragment = null;
		if (session != null && session.isOpened()) {
			// if the session is already open,
			// try to show the selection fragment
			Log.i("main activity", "on resume fargment");
			if (savedInstanceState != null) {
				savedFragment = manager.getFragment(savedInstanceState,
						"saveFragment");
				Log.i("main activity", "on resume fargment"
						+ savedInstanceState);
			}
			if (savedFragment == null) {
				Log.i("main activity", "on resume fargment no saved fragment");
				FragmentTransaction trans = manager.beginTransaction();

				trans.replace(R.id.fragment_placeholder,
						new ValidateUserFragment()).commitAllowingStateLoss();

			} else {

				manager.beginTransaction()
						.replace(R.id.fragment_placeholder, savedFragment)
						.commitAllowingStateLoss();
			}

		} else {
			// otherwise present the splash screen
			// and ask the person to login.
			FragmentTransaction trans1 = manager.beginTransaction();
			trans1.replace(R.id.fragment_placeholder, new LogInFragment())
					.commitAllowingStateLoss();

		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// only add the menu when the login fragment is not showing
		if (menu.size() == 0) {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.menu, menu);
			settings = menu.getItem(0);
			return true;
		} else {
			settings = null;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.equals(settings)) {
			mySlidingMenu.toggle();
			return true;
		}
		return false;
	}

	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU) {
	// Class<? extends Object> currentFragmentClass =
	// getSupportFragmentManager()
	// .findFragmentById(R.id.fragment_placeholder).getClass();
	// if (currentFragmentClass != LogInFragment.class
	// && currentFragmentClass != ValidateUserFragment.class) {
	// if (mySlidingMenu.isMenuShowing())
	// mySlidingMenu.toggle();
	// else
	// mySlidingMenu.toggle();
	// }
	// return true;
	// } else {
	// return super.onKeyUp(keyCode, event);
	// }
	// }

	@Override
	public void onBackPressed() {
		Log.w("BACK PRESSED", backpressCount + "");
		if (mySlidingMenu.isMenuShowing()) {
			mySlidingMenu.toggle();
		} else {
			FragmentManager fm = getSupportFragmentManager();

			int count = fm.getBackStackEntryCount();
			if (count != 0) {
				fm.popBackStack();
				backpressCount--;
				if (backpressCount < 0)
					backpressCount = 0;
			} else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

				if (backpressCount == 0) {
					Toast.makeText(MainActivity.this,
							"Press back again to exit", Toast.LENGTH_SHORT)
							.show();
					backpressCount++;
				} else {
					backpressCount++;
				}
				// or just go back to main activity
				if (backpressCount >= 2) {
					// super.onBackPressed();
					MainActivity.this.finish();
					System.exit(0);
				}
			}
		}
	}
}
