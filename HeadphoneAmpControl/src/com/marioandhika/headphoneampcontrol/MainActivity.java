package com.marioandhika.headphoneampcontrol;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener, OnSeekBarChangeListener{

	private CheckBox checkBoxToggleService;
	private SharedPreferences sp;
	private SharedPreferences.Editor spe;
	
	private MainFragment mainFragment;
	private CheckBox checkBoxToggleSafety;
	private CheckBox checkBoxVolumeButtonHack;
	private SeekBar seekBarMinLevel;
	private SeekBar seekBarMaxLevel;
	private SeekBar seekBarSafetyLevel;
	private TextView textViewMinLevel;
	private TextView textViewMaxLevel;
	private TextView textViewSafetyLevel;
	private Button buttonCommitChanges;
	private SeekBar seekBarVolumeButtonHack;
	private TextView textViewVolumeButtonHack;
	private CheckBox checkBoxMusicHack;
	private CheckBox checkBoxVoiceCallHack;
	private CheckBox checkBoxRingHack;
	
	public static final int MIN_LEVEL = 0;
	public static final int MAX_LEVEL = 63;
	public static final int LEVEL_OFFSET = -57;
	
	public static final String CHECKBOX_SERVICE_CHECKED = "checkbox_service_checked";
	public static final String CHECKBOX_SAFETY_CHECKED = "checkbox_safety_checked";
	public static final String SEEKBAR_MIN_LEVEL = "seekbar_min_level";
	public static final String SEEKBAR_MAX_LEVEL = "seekbar_max_level";
	public static final String SEEKBAR_SAFETY_LEVEL = "seekbar_safety_level";
	public static final String CHECKBOX_BALANCE = "checkbox_balance";
	public static final String CHECKBOX_VOLUME_BUTTON_HACK = "checkbox_volume_button_hack";
	public static final int TOGGLE_VOLUME_HACK = 9;
	public static final String SEEKBAR_VOLUME_BUTTON_HACK = "seekbar_volume_button_hack";
	public static final String CHECKBOX_RING_HACK = "checkbox_ring_hack";
	public static final String CHECKBOX_VOICE_CALL_HACK = "checkbox_voice_call_hack";
	public static final String CHECKBOX_MUSIC_HACK = "checkbox_music_hack";	
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get views
		checkBoxToggleService = (CheckBox) findViewById(R.id.checkBox_toggleService);
		checkBoxToggleSafety = (CheckBox) findViewById(R.id.checkBox_toggleSafetyLevel);
		checkBoxToggleService.setOnCheckedChangeListener(this);
		checkBoxToggleSafety.setOnCheckedChangeListener(this);
		checkBoxVolumeButtonHack = (CheckBox) findViewById(R.id.checkBox_VolumeButtonHack);
		checkBoxVolumeButtonHack.setOnCheckedChangeListener(this);
		checkBoxMusicHack = (CheckBox) findViewById(R.id.checkBox_musicHack);
		checkBoxVoiceCallHack = (CheckBox) findViewById(R.id.checkBox_voiceCallHack);
		checkBoxRingHack = (CheckBox) findViewById(R.id.checkBox_ringHack);
		checkBoxMusicHack.setOnCheckedChangeListener(this);
		checkBoxVoiceCallHack.setOnCheckedChangeListener(this);
		checkBoxRingHack.setOnCheckedChangeListener(this);
		
		seekBarMinLevel = (SeekBar) findViewById(R.id.seekBar_minLevel);
		seekBarMaxLevel = (SeekBar) findViewById(R.id.seekBar_maxLevel);
		seekBarSafetyLevel = (SeekBar) findViewById(R.id.seekBar_safetyLevel);
		seekBarVolumeButtonHack = (SeekBar) findViewById(R.id.seekBar_VolumeButtonHack);
		seekBarMinLevel.setOnSeekBarChangeListener(this);
		seekBarMaxLevel.setOnSeekBarChangeListener(this);
		seekBarSafetyLevel.setOnSeekBarChangeListener(this);
		seekBarVolumeButtonHack.setOnSeekBarChangeListener(this);
		
		textViewMinLevel = (TextView) findViewById(R.id.textView_minLevel);
		textViewMaxLevel = (TextView) findViewById(R.id.textView_maxLevel);
		textViewSafetyLevel = (TextView) findViewById(R.id.textView_safetyLevel);
		textViewVolumeButtonHack = (TextView) findViewById(R.id.textView_VolumeButtonHack);
		
//		buttonCommitChanges = (Button) findViewById(R.id.button_commitChanges);
//		buttonCommitChanges.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				while (!spe.commit()){}
//				mainFragment.rebuildSeekbar();
//			}
//			
//		});
		
		// Initialize based on preferences
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		spe = sp.edit();
		
		boolean isToggleServiceChecked = sp.getBoolean(CHECKBOX_SERVICE_CHECKED, false);
		boolean isToggleSafetyChecked = sp.getBoolean(CHECKBOX_SAFETY_CHECKED, false);
		boolean isVolumeButtonHackChecked = sp.getBoolean(CHECKBOX_VOLUME_BUTTON_HACK, false);
		int minLevel = sp.getInt(SEEKBAR_MIN_LEVEL, MIN_LEVEL);
		int maxLevel = sp.getInt(SEEKBAR_MAX_LEVEL, MAX_LEVEL);
		int safetyLevel = sp.getInt(SEEKBAR_SAFETY_LEVEL, MIN_LEVEL);
		int hackLevelJump = sp.getInt(SEEKBAR_VOLUME_BUTTON_HACK, 1);
		boolean isMusicHackChecked = sp.getBoolean(CHECKBOX_MUSIC_HACK, false);
		boolean isVoiceCallHackChecked = sp.getBoolean(CHECKBOX_VOICE_CALL_HACK, false);
		boolean isRingHackChecked = sp.getBoolean(CHECKBOX_RING_HACK, false);
		
		checkBoxMusicHack.setChecked(isMusicHackChecked);
		checkBoxVoiceCallHack.setChecked(isVoiceCallHackChecked);
		checkBoxRingHack.setChecked(isRingHackChecked);
		checkBoxVolumeButtonHack.setChecked(isVolumeButtonHackChecked);
		checkBoxVolumeButtonHack.setEnabled(isToggleServiceChecked);
		checkBoxToggleService.setChecked(isToggleServiceChecked);
		checkBoxToggleSafety.setChecked(isToggleSafetyChecked);
		//seekBarSafetyLevel.setEnabled(isToggleSafetyChecked);
		checkBoxToggleSafety.setEnabled(isToggleServiceChecked);
		//seekBarSafetyLevel.setEnabled(checkBoxToggleSafety.isChecked() && checkBoxToggleSafety.isEnabled());
		
		checkBoxMusicHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());
		checkBoxVoiceCallHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());
		checkBoxRingHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());
		
		seekBarVolumeButtonHack.setProgress(hackLevelJump);
		seekBarMaxLevel.setProgress(maxLevel);
		seekBarSafetyLevel.setProgress(safetyLevel);
		textViewMinLevel.setText("Min level: " + minLevel);
		textViewMaxLevel.setText("Max level: " + maxLevel);
		textViewSafetyLevel.setText("Safety level: " + safetyLevel);
		textViewVolumeButtonHack.setText("Amp level jump: " + (hackLevelJump+1));
		
		// Get fragment for later access
		FragmentManager fm = getFragmentManager();
		mainFragment = (MainFragment) fm.findFragmentById(R.id.main_fragment);
		
		seekBarMinLevel.setProgress(minLevel);
		
		Log.d("DBG","Tst");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void toggleService(boolean isChecked) {
		Intent service = new Intent(this, MainService.class);
		spe.putBoolean(CHECKBOX_SERVICE_CHECKED, isChecked);
		while (!spe.commit()){}
		spe.apply();
		
		if (isChecked) {
			// Start service
			if (!isMyServiceRunning()) {
				startService(service);
			}
		} else {
			// Stop service
			stopService(service);
		}

		checkBoxVolumeButtonHack.setEnabled(isChecked);
		

		checkBoxMusicHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		checkBoxVoiceCallHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		checkBoxRingHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		
		checkBoxToggleSafety.setEnabled(isChecked);
		//seekBarSafetyLevel.setEnabled(checkBoxToggleSafety.isChecked() && checkBoxToggleSafety.isEnabled());
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (MainService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void toggleSafety(boolean isChecked) {
		// TODO Auto-generated method stub
		
		if (seekBarSafetyLevel.getProgress() > seekBarMaxLevel.getProgress()) {
			seekBarSafetyLevel.setProgress(seekBarMaxLevel.getProgress());
		} else 	if (seekBarSafetyLevel.getProgress() < seekBarMinLevel.getProgress()) {
			seekBarSafetyLevel.setProgress(seekBarMinLevel.getProgress());
		} else {
			textViewSafetyLevel.setText("Safety level: " + seekBarSafetyLevel.getProgress());
		}

		//seekBarSafetyLevel.setEnabled(isChecked);
		
		
		spe.putBoolean(CHECKBOX_SAFETY_CHECKED, isChecked);
		spe.apply();

		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch (buttonView.getId()) {
			case R.id.checkBox_toggleService:
				toggleService(isChecked);
				break;
			case R.id.checkBox_toggleSafetyLevel:
				toggleSafety(isChecked);
				break;
			case R.id.checkBox_VolumeButtonHack:
				toggleVolumeButtonHack(isChecked);
				break;
			case R.id.checkBox_musicHack:
				spe.putBoolean(CHECKBOX_MUSIC_HACK, isChecked);
				while (!spe.commit()){}
				break;
			case R.id.checkBox_voiceCallHack:
				spe.putBoolean(CHECKBOX_VOICE_CALL_HACK, isChecked);
				while (!spe.commit()){}
				break;
			case R.id.checkBox_ringHack:
				spe.putBoolean(CHECKBOX_RING_HACK, isChecked);
				while (!spe.commit()){}
				break;
		}
	}


	private void toggleVolumeButtonHack(boolean isChecked) {
		spe.putBoolean(CHECKBOX_VOLUME_BUTTON_HACK, isChecked);
		spe.apply();
		
		checkBoxMusicHack.setEnabled(isChecked);
		checkBoxVoiceCallHack.setEnabled(isChecked);
		checkBoxRingHack.setEnabled(isChecked);
		
		Intent service = new Intent(this, MainService.class);
		service.putExtra(MainService.HEADSET_STATUS, MainActivity.TOGGLE_VOLUME_HACK);
		startService(service);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.seekBar_minLevel:
				if (progress > seekBarMaxLevel.getProgress()) {
					seekBar.setProgress(seekBarMaxLevel.getProgress());
				} else if (progress > seekBarSafetyLevel.getProgress() && checkBoxToggleSafety.isChecked()) {
					seekBar.setProgress(seekBarSafetyLevel.getProgress());
				} else {
					textViewMinLevel.setText("Min level: " + progress);
				}
				break;
			case R.id.seekBar_maxLevel:
				if (progress < seekBarMinLevel.getProgress()) {
					seekBar.setProgress(seekBarMinLevel.getProgress());
				} else if (progress < seekBarSafetyLevel.getProgress() && checkBoxToggleSafety.isChecked()) {
					seekBar.setProgress(seekBarSafetyLevel.getProgress());
				} else {
					textViewMaxLevel.setText("Max level: " + progress);
				}
				break;
			case R.id.seekBar_safetyLevel:
				if (progress > seekBarMaxLevel.getProgress()) {
					seekBar.setProgress(seekBarMaxLevel.getProgress());
				} else 	if (progress < seekBarMinLevel.getProgress()) {
					seekBar.setProgress(seekBarMinLevel.getProgress());
				} else {
					textViewSafetyLevel.setText("Safety level: " + progress);
				}
				break;
			case R.id.seekBar_VolumeButtonHack:
				textViewVolumeButtonHack.setText("Amp level jump: " + (progress+1));
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
//		switch (seekBar.getId()) {
//			case R.id.seekBar_minLevel:
		
//				break;
//			case R.id.seekBar_maxLevel:
//				break;
//			case R.id.seekBar_safetyLevel:
//				break;
//		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.seekBar_minLevel:
				spe.putInt(SEEKBAR_MIN_LEVEL, seekBar.getProgress());
				while (!spe.commit()){}
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_maxLevel:
				spe.putInt(SEEKBAR_MAX_LEVEL, seekBar.getProgress());
				while (!spe.commit()){}
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_safetyLevel:
				spe.putInt(SEEKBAR_SAFETY_LEVEL, seekBar.getProgress());
				while (!spe.commit()){}
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_VolumeButtonHack:
				spe.putInt(SEEKBAR_VOLUME_BUTTON_HACK, seekBar.getProgress());
				while (!spe.commit()){}
				Intent service = new Intent(this, MainService.class);
				service.putExtra(MainService.HEADSET_STATUS, MainActivity.TOGGLE_VOLUME_HACK);
				startService(service);
				break;
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mainFragment.rebuildSeekbar();
	}
}
