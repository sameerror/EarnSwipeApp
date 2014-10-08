package com.nurakanbpo.earnswipe;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nurakanbpo.earnswipe.helper.SomeImportantMethods;

public class TestAnimation extends Activity {
	AnimationDrawable ringAnimationDrawable;
	ImageView ringImageView;
	SomeImportantMethods importantMethods;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		// ringImageView = (ImageView) findViewById(R.id.ringImage);

		importantMethods = new SomeImportantMethods(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			importantMethods.runRingAnimation();
			return true;
		}

		return super.onTouchEvent(event);
	}

}
