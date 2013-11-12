package com.marioandhika.headphoneampcontrol;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

public class DialogActivity extends Activity {
	
	//public static final String STARTED_FROM_VOLUME_BUTTONS = "started_from_volume_buttons";
	public static final int INCREASE = 1;
	public static final int DECREASE = 2;
	public static final int NEITHER = 0;
	public static final String DECREASE_OR_INCREASE = "decrease_or_increase";
	public static final String ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR = "action_refresh_headphone_level_seekbar";
	
	private MainFragment mf;
	private VolumeChangeReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog);
		ViewGroup v = (ViewGroup) findViewById(R.id.activity_dialog);
		ColorDrawable dialogColor = new ColorDrawable(Color.BLACK);
		dialogColor.setAlpha(192);//192);
		getWindow().setBackgroundDrawable(dialogColor);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.gravity = Gravity.BOTTOM;
		//lp.windowAnimations = 0;
	    getWindow().setAttributes(lp);
		//v.setAlpha(0.9f);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    
		FragmentManager fm = getFragmentManager();
		mf = (MainFragment) fm.findFragmentById(R.id.main_fragment);
	
		//boolean startedFromVolumeButtons = getIntent().getBooleanExtra(STARTED_FROM_VOLUME_BUTTONS, false);
		//boolean mainServiceForeground = getIntent().getBooleanExtra(MainService.MAIN_SERVICE_FOREGROUND, false);
		
		int decreaseOrIncrease = getIntent().getIntExtra(DECREASE_OR_INCREASE, NEITHER);
		
		if (decreaseOrIncrease != NEITHER) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
			receiver = new VolumeChangeReceiver();
			registerReceiver(receiver, filter);
		}
//		switch (decreaseOrIncrease) {
//			case INCREASE:
//				mf.increaseLevel();
//				break;
//			case DECREASE:
//				mf.decreaseLevel();
//				break;
//		}
		
//		if (mainServiceForeground) {
//			// Prevent volume change, max them out
//			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//			am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//		}
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		switch (keyCode) {
//			case KeyEvent.KEYCODE_VOLUME_DOWN:
//				mf.decreaseLevel();
//				break;
//			case KeyEvent.KEYCODE_VOLUME_UP:
//				mf.increaseLevel();
//				break;
//		}
//		
//	    return true;
//	}
	
	public void onDestroy(){
		if (receiver != null){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}
	
	public class VolumeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mf.rebuildSeekbar();
			mf.resetTimer();
		}
		
	}
}