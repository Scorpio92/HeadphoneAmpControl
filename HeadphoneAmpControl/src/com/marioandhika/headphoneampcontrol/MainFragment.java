package com.marioandhika.headphoneampcontrol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnSeekBarChangeListener, OnClickListener, OnCheckedChangeListener{
	public static final String FILE_AMP_LEVEL_LEFT = "/sys/class/misc/wolfson_control/headphone_left";//"/sys/class/misc/scoobydoo_sound/headphone_amplifier_level";
	public static final String FILE_AMP_LEVEL_RIGHT = "/sys/class/misc/wolfson_control/headphone_right";
	private TextView textViewLevel;
	private SeekBar seekBarMain;
	private Handler idleKillTimer;
	private IdleTimer idleTimerRunnable;
	
	private int minLevel;
	private int maxLevel;
	private int currentRightLevel;
	private int currentLeftLevel;
	private SeekBar seekBarMainRight;
	private TextView textViewLevelRight;
	private CheckBox checkBoxBalance;
	private boolean isBalanced;
	
	public static final int MIN_LEVEL = 0;
	public static final int MAX_LEVEL = 63;
	public static final int LEVEL_OFFSET = -57;
	public static final long FINISH_DELAY = 2000L;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.control, container, false);
        // Inflate the layout for this fragment
		textViewLevel = (TextView) v.findViewById(R.id.textView_level);
		textViewLevelRight = (TextView) v.findViewById(R.id.textView_levelRight);
		seekBarMain= (SeekBar) v.findViewById(R.id.seekBar_main);
		seekBarMainRight= (SeekBar) v.findViewById(R.id.seekBar_mainRight);
		checkBoxBalance = (CheckBox) v.findViewById(R.id.checkBox_Balance);
		
		seekBarMain.setOnSeekBarChangeListener(this);
		seekBarMainRight.setOnSeekBarChangeListener(this);
		

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		isBalanced = sp.getBoolean(MainActivity.CHECKBOX_BALANCE, false);
		checkBoxBalance.setChecked(isBalanced);
		
		checkBoxBalance.setOnCheckedChangeListener(this);
		

		
		rebuildSeekbar();

		idleKillTimer = new Handler(); // Make your Main UIWorker Thread to execute this statement
		idleTimerRunnable = new IdleTimer();
		if (getActivity().getClass() == DialogActivity.class) {
			idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
		}
		
        return v;
    }
	
	

	private void fixPermissions() {
		// Modify file permissions
		String[] hin1 = { "su", "-c",
				"chmod o+w " + FILE_AMP_LEVEL_LEFT,
				"chmod o+w " + FILE_AMP_LEVEL_RIGHT };
		try {
			Runtime.getRuntime().exec(hin1);
		} catch (IOException e) {
			// Handle unrooted devices
			Toast.makeText(getActivity(), "Root access required for HeadphoneAmpControl to work", Toast.LENGTH_LONG).show();
			getActivity().finish();
			e.printStackTrace();
		}
	}

	public void rebuildSeekbar(){
		File mFileL = new File(MainFragment.FILE_AMP_LEVEL_LEFT);
		File mFileR = new File(MainFragment.FILE_AMP_LEVEL_RIGHT);
		if (!mFileL.canWrite() || !mFileR.canWrite()) {
			fixPermissions();
		}

		try {
			Scanner scannerL = new Scanner(mFileL);
			Scanner scannerR = new Scanner(mFileR);
			currentLeftLevel = scannerL.nextInt();
			currentRightLevel = scannerR.nextInt();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		minLevel = sp.getInt(MainActivity.SEEKBAR_MIN_LEVEL, MIN_LEVEL);
		maxLevel = sp.getInt(MainActivity.SEEKBAR_MAX_LEVEL, MAX_LEVEL);
		
		seekBarMain.setMax(maxLevel-minLevel);
		seekBarMainRight.setMax(maxLevel-minLevel);
		
		if (currentLeftLevel > maxLevel) {
			currentLeftLevel = maxLevel;
		} else if (currentLeftLevel < minLevel) {
			currentLeftLevel = minLevel;
		}
		
		if (currentRightLevel > maxLevel) {
			currentRightLevel = maxLevel;
		} else if (currentRightLevel < minLevel) {
			currentRightLevel = minLevel;
		}

		if (isBalanced) {
			seekBarMainRight.setVisibility(View.GONE);
			textViewLevelRight.setVisibility(View.GONE);
			
			currentRightLevel = currentLeftLevel;
			setLevel(currentLeftLevel, currentRightLevel);
			updateSeekbar(currentLeftLevel, currentRightLevel);
			updateLabel(currentLeftLevel, currentRightLevel);
		} else {
			seekBarMainRight.setVisibility(View.VISIBLE);
			textViewLevelRight.setVisibility(View.VISIBLE);
			
			setLevel(currentLeftLevel, currentRightLevel);
			updateSeekbar(currentLeftLevel, currentRightLevel);
			updateLabel(currentLeftLevel, currentRightLevel);
		}
		
	}
	
	private void updateSeekbar(int currentLevel2, int currentRightLevel2) {
		// TODO Auto-generated method stub
		seekBarMain.setProgress(currentLevel2-minLevel);
		seekBarMainRight.setProgress(currentRightLevel2-minLevel);
	}

	private void updateLabel(int currentLevel2, int currentRightLevel2) {
		textViewLevel.setText(String.valueOf(currentLevel2));
		textViewLevelRight.setText(String.valueOf(currentRightLevel2));
		//textViewLevel.setText(String.valueOf(currentLevel2 + LEVEL_OFFSET) + " dB");
	}
	
	public void setLevel(int level, int currentRightLevel2) {
		File mFile = new File(MainFragment.FILE_AMP_LEVEL_LEFT);
		FileOutputStream mFos;
		try {
			mFos = new FileOutputStream(mFile);

			byte[] bytesToWrite = String.valueOf(level).getBytes();
			mFos.write(bytesToWrite);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File mFileR = new File(MainFragment.FILE_AMP_LEVEL_RIGHT);
		FileOutputStream mFosR;
		try {
			mFosR = new FileOutputStream(mFileR);

			byte[] bytesToWrite = String.valueOf(currentRightLevel2).getBytes();
			mFosR.write(bytesToWrite);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateNotification() {
//		// TODO Auto-generated method stub
//
//    	Intent notificationIntent = new Intent(getActivity(), DialogActivity.class);
//    	PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		
//		Notification noti = new Notification.Builder(getActivity())
//    	.setContentTitle("Headphone Amp")
//    	.setContentText("Amp level: " + currentLevel2)
//    	.setSmallIcon(R.drawable.ic_launcher)
//        .setContentIntent(pendingIntent)
//        .setContentInfo("")
//        .setWhen(0)
//        .build();
//		
//		NotificationManager nm = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
//		nm.notify(MainService.ONGOING_NOTIFICATION, noti);
		
		Intent service = new Intent(getActivity(), MainService.class);
		getActivity().startService(service);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (fromUser) {
			if (isBalanced) {
				updateLabel(progress+minLevel, progress+minLevel);
				setLevel(progress+minLevel, progress+minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
			} else {
				switch (seekBar.getId()) {
					case R.id.seekBar_main:
						updateLabel(progress+minLevel, seekBarMainRight.getProgress()+minLevel);
						setLevel(progress+minLevel, seekBarMainRight.getProgress()+minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
						break;
					case R.id.seekBar_mainRight:
						updateLabel(seekBarMain.getProgress()+minLevel, progress+minLevel);
						setLevel(seekBarMain.getProgress()+minLevel, progress+minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
						break;
				}
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		idleKillTimer.removeCallbacks(idleTimerRunnable);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		//int progress = seekBar.getProgress();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean isServiceChecked = sp.getBoolean(MainActivity.CHECKBOX_SERVICE_CHECKED, false);
		
		if (isServiceChecked) {
			updateNotification();
		}
		//setLevel(progress+minLevel); // Uncomment for non-immediate level change. See onProgressChanged().
		if (getActivity().getClass() == DialogActivity.class) {
			idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		switch (v.getId()) {
//			case R.id.button_levelDown:
//				decreaseLevel(v);
//				break;
//			case R.id.button_levelUp:
//				increaseLevel(v);
//				break;
//        }
	}

	@Override
	public void onPause () {
		idleKillTimer.removeCallbacks(idleTimerRunnable);
		
		super.onPause();
	}
	
	private class IdleTimer implements Runnable {
		 @Override
		 public void run() {
			 if (this != null && getActivity() != null)
				 getActivity().finish();  
		 }
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (isChecked) {
			sp.edit().putBoolean(MainActivity.CHECKBOX_BALANCE, true).commit();
			isBalanced = true;
			//setLevel(seekBarMain.getProgress(), seekBarMain.getProgress());
			//updateLabel(seekBarMain.getProgress(), seekBarMain.getProgress());
			
		} else {
			sp.edit().putBoolean(MainActivity.CHECKBOX_BALANCE, false).commit();
			isBalanced = false;
		}
		rebuildSeekbar();
		//Toast.makeText(getActivity(), "HAHAHAH" + sp.getBoolean(MainActivity.CHECKBOX_BALANCE, false), Toast.LENGTH_SHORT).show();
		if (getActivity().getClass() == DialogActivity.class) {
			resetTimer();
		}
		
	}

	public void decreaseLevel() {

		if (currentLeftLevel > minLevel && currentRightLevel > minLevel) {
			// decrease level
			setLevel(--currentLeftLevel, --currentRightLevel);
			updateLabel(currentLeftLevel, currentRightLevel);
			updateSeekbar(currentLeftLevel, currentRightLevel);
			updateNotification();
		}
		if (getActivity().getClass() == DialogActivity.class) {
			resetTimer();
		}		
	}

	public void increaseLevel() {

		if (currentLeftLevel < maxLevel && currentRightLevel < maxLevel) {
			// increase level
			setLevel(++currentLeftLevel, ++currentRightLevel);
			updateLabel(currentLeftLevel, currentRightLevel);
			updateSeekbar(currentLeftLevel, currentRightLevel);
			updateNotification();
		}
		
		if (getActivity().getClass() == DialogActivity.class) {
			resetTimer();
		}
	}
	
	public void resetTimer(){
		idleKillTimer.removeCallbacks(idleTimerRunnable);
		idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
	}
}
