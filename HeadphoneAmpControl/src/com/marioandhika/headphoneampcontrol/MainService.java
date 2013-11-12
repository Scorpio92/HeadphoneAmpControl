package com.marioandhika.headphoneampcontrol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {

	public static final int ONGOING_NOTIFICATION = 9999;

	public static final String HEADSET_STATUS = "HeadsetStatus";

	//public static final String MAIN_SERVICE_FOREGROUND = "main_service_foreground";

	private MainReceiver receiver;

	private int headsetStatus;

	//	private SettingsContentObserver mSettingsContentObserver;

	private int currentMusicVolume;

	public AudioManager am;

	private int currentLevelL;

	private int currentLevelR;

	private VolumeChangeReceiver vr;

	private SharedPreferences sp;

	private IntentFilter filterV;

	private Integer hackLevelJump;

	public int currentVoiceCallVolume;

	public int currentRingVolume;
	private boolean isForeground;

	private int safetyLevel;

	@Override
	public void onCreate() {
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		currentVoiceCallVolume = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		currentRingVolume = am.getStreamVolume(AudioManager.STREAM_RING);
		isForeground = false;
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		receiver = new MainReceiver();
		registerReceiver(receiver, filter);

		filterV = new IntentFilter();
		vr = new VolumeChangeReceiver();
		filterV.addAction("android.media.VOLUME_CHANGED_ACTION");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int intentHeadsetStatus;

		if (intent != null) {
			intentHeadsetStatus = intent.getIntExtra(HEADSET_STATUS, MainReceiver.UNALTERED);

			if (intentHeadsetStatus != MainReceiver.UNALTERED) {
				headsetStatus = intentHeadsetStatus;
			}
		} else {
			intentHeadsetStatus = MainReceiver.UNALTERED;
		}

		switch (intentHeadsetStatus) {
			case MainReceiver.UNPLUGGED:
				deactivateNotification();
				break;
			case MainReceiver.PLUGGED:
				activateNotification();
				break;
			case MainReceiver.UNALTERED:
				updateNotification();
				break;
			case MainActivity.TOGGLE_VOLUME_HACK:
				toggleVolumeHack();
				break;
		}

		return Service.START_STICKY;
	}

	private void toggleVolumeHack() {
		if (isForeground) {
			if (sp.getBoolean(MainActivity.CHECKBOX_VOLUME_BUTTON_HACK, false)) {
				registerReceiver(vr,filterV);
				hackLevelJump = sp.getInt(MainActivity.SEEKBAR_VOLUME_BUTTON_HACK, 1) + 1;
				
				if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {
					am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
				}
				if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
					am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)-1, AudioManager.FLAG_SHOW_UI);
				}
				if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
					am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING)-1, AudioManager.FLAG_SHOW_UI);
				}
			} else {
				if (vr != null) {
					try {
						unregisterReceiver(vr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private void updateNotification() {
		if (headsetStatus == MainReceiver.PLUGGED) {

			File mFileL = new File(MainFragment.FILE_AMP_LEVEL_LEFT);
			File mFileR = new File(MainFragment.FILE_AMP_LEVEL_RIGHT);

			if (!mFileL.canWrite() || !mFileR.canWrite()) {
				fixPermissions();
			}

			try {
				Scanner scannerL = new Scanner(mFileL);
				Scanner scannerR = new Scanner(mFileR);
				currentLevelL = scannerL.nextInt();
				currentLevelR = scannerR.nextInt();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			Intent notificationIntent = new Intent(this, DialogActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification noti = new Notification.Builder(this)
			.setContentTitle("Headphone Amp")
			.setContentText("Amp level: L:" + currentLevelL + " R:" + currentLevelR)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(pendingIntent)
			.setContentInfo("")
			.setOngoing(true)
			.setWhen(0)
			.build();

			NotificationManager nm =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			nm.notify(ONGOING_NOTIFICATION, noti);
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		if (vr != null) {
			try {
				unregisterReceiver(vr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//    	if (mSettingsContentObserver != null) {
		//    		this.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver); 
		//    	}
	}

	private void activateNotification(){

		File mFileL = new File(MainFragment.FILE_AMP_LEVEL_LEFT);
		File mFileR = new File(MainFragment.FILE_AMP_LEVEL_RIGHT);

		if (!mFileL.canWrite() || !mFileR.canWrite()) {
			fixPermissions();
		}

		try {
			Scanner scannerL = new Scanner(mFileL);
			Scanner scannerR = new Scanner(mFileR);
			currentLevelL = scannerL.nextInt();
			currentLevelR = scannerR.nextInt();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean isSafetyChecked = sp.getBoolean(MainActivity.CHECKBOX_SAFETY_CHECKED, false);
		if (isSafetyChecked) {
			safetyLevel = sp.getInt(MainActivity.SEEKBAR_SAFETY_LEVEL, MainActivity.MIN_LEVEL);

			if (currentLevelL > safetyLevel || currentLevelR > safetyLevel){

				if (currentLevelL > safetyLevel) {
					currentLevelL = safetyLevel;
				}
				if (currentLevelR > safetyLevel) {
					currentLevelR = safetyLevel;
				}
				Toast.makeText(this, "HP Amp set to safety levels L&R: "+safetyLevel, Toast.LENGTH_SHORT).show();
				FileOutputStream mFos;
				try {
					mFos = new FileOutputStream(mFileL);

					byte[] bytesToWrite = String.valueOf(currentLevelL).getBytes();
					mFos.write(bytesToWrite);


					mFos = new FileOutputStream(mFileR);


					bytesToWrite = String.valueOf(currentLevelR).getBytes();
					mFos.write(bytesToWrite);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Intent notificationIntent = new Intent(this, DialogActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		//NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		//nm.cancel(ONGOING_NOTIFICATION);
		Notification noti = new Notification.Builder(this)
		.setContentTitle("Headphone Amp")
		.setContentText("Amp level: L:" + currentLevelL + " R:" + currentLevelR)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentIntent(pendingIntent)
		.setContentInfo("")
		.setOngoing(true)
		.setWhen(0)
		.build();

		startForeground(ONGOING_NOTIFICATION, noti);



		//   	 Register volume change listener
		//   	mSettingsContentObserver = new SettingsContentObserver( new Handler() ); 
		//   	this.getApplicationContext().getContentResolver().registerContentObserver( 
		//   			android.provider.Settings.System.CONTENT_URI,
		//               true, 
		//   	    mSettingsContentObserver );

		if (sp.getBoolean(MainActivity.CHECKBOX_VOLUME_BUTTON_HACK, false)) {
			registerReceiver(vr,filterV);
			hackLevelJump = sp.getInt(MainActivity.SEEKBAR_VOLUME_BUTTON_HACK, 1) + 1;
			//	Log.d("MION","CUTE THIGH AND SHOULDERS");
			if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {
				am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
			}
			if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
				am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)-1, AudioManager.FLAG_SHOW_UI);
			}
			if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
				am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING)-1, AudioManager.FLAG_SHOW_UI);
			}
		}

		isForeground = true;
	}


	private void deactivateNotification() {
		stopForeground(true);
		if (vr != null) {
			try {
				unregisterReceiver(vr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//    	if (mSettingsContentObserver != null) {
		//    		this.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver); 
		//    	}
		isForeground = false;
	}

	private void fixPermissions() {
		// Modify file permissions
		String[] hin1 = { "su", "-c",
				"chmod o+w " + MainFragment.FILE_AMP_LEVEL_LEFT,
				"chmod o+w " + MainFragment.FILE_AMP_LEVEL_RIGHT };
		try {
			Runtime.getRuntime().exec(hin1);
		} catch (IOException e) {
			// Handle unrooted devices
			Toast.makeText(this, "Root access required for HeadphoneAmpControl to work", Toast.LENGTH_LONG).show();
			stopSelf();
			e.printStackTrace();
		}
	}

	private void launchDialogActivity(int decreaseOrIncrease) {
		// Launch dialog activity
		Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(DialogActivity.DECREASE_OR_INCREASE, decreaseOrIncrease);
		//intent.putExtra(MainService.MAIN_SERVICE_FOREGROUND, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);

	}
	//    
	//    public class SettingsContentObserver extends ContentObserver {
	//
	//		public SettingsContentObserver(Handler handler) {
	//		    super(handler);
	//		} 
	//
	//		@Override
	//		public boolean deliverSelfNotifications() {
	//		     return false;//return super.deliverSelfNotifications(); 
	//		}
	//
	//		@Override
	//		public void onChange(boolean selfChange) {
	//		    super.onChange(selfChange);
	//		    //if (!selfChange){
	//		    	if (am.getStreamVolume(AudioManager.STREAM_MUSIC) != currentMusicVolume){
	//
	//		    		if (am.getStreamVolume(AudioManager.STREAM_MUSIC) > currentMusicVolume) {
	//
	//		    			am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
	//		    			currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	//		    			launchDialogActivity(DialogActivity.INCREASE);
	//		    			//Log.d("1111","1111111 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
	//		    			increaseLevel();
	//		    		} else if (am.getStreamVolume(AudioManager.STREAM_MUSIC) < currentMusicVolume) {
	//
	//		    			am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
	//		    			currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	//		    			launchDialogActivity(DialogActivity.DECREASE);
	//
	//		    			decreaseLevel();
	//		    			
	//		    		}
	//		    		//System.gc();
	//		    		//Log.d("222222","222222222 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
	//		    		//currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	//		    	}
	//		    	//Log.d("3333333","333333333 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
	//		    	am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
	//    			
	//		    //}
	//		    //am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
	//		}
	//
	//    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	public void setLevel(int currentLeftLevel2, int currentRightLevel2) {
		File mFile = new File(MainFragment.FILE_AMP_LEVEL_LEFT);
		FileOutputStream mFos;
		try {
			mFos = new FileOutputStream(mFile);

			byte[] bytesToWrite = String.valueOf(currentLeftLevel2).getBytes();
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

	private void decreaseLevel() {
		// TODO Auto-generated method stub
		setLevel(currentLevelL-=hackLevelJump, currentLevelR-=hackLevelJump);
		updateNotification();
		Intent intent = new Intent(DialogActivity.ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
		sendBroadcast(intent);
	}

	private void increaseLevel() {
		// TODO Auto-generated method stub
		setLevel(currentLevelL+=hackLevelJump, currentLevelR+=hackLevelJump);
		updateNotification();
		Intent intent = new Intent(DialogActivity.ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
		sendBroadcast(intent);
	}

	public class VolumeChangeReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.d("1111",intent.getExtras().toString());
			//int newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
			int minLevel = sp.getInt(MainActivity.SEEKBAR_MIN_LEVEL, 0);

			if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {

				if (am.getStreamVolume(AudioManager.STREAM_MUSIC) != currentMusicVolume){
					//if (newVolume > 0){

					if (am.getStreamVolume(AudioManager.STREAM_MUSIC) >= currentMusicVolume) {

						am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
						currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
						launchDialogActivity(DialogActivity.INCREASE);
						//Log.d("1111","1111111 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
						increaseLevel();
					} else if (am.getStreamVolume(AudioManager.STREAM_MUSIC) < currentMusicVolume) {
						if (currentLevelL > minLevel && currentLevelR > minLevel) {
							am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1, AudioManager.FLAG_SHOW_UI);
							currentMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
							launchDialogActivity(DialogActivity.DECREASE);

							decreaseLevel();

						}
					}
				}
			}

			if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
				if (am.getStreamVolume(AudioManager.STREAM_VOICE_CALL) != currentVoiceCallVolume){
					//if (newVolume > 0){

					if (am.getStreamVolume(AudioManager.STREAM_VOICE_CALL) >= currentVoiceCallVolume) {

						am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)-1, AudioManager.FLAG_SHOW_UI);
						currentVoiceCallVolume = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
						launchDialogActivity(DialogActivity.INCREASE);
						//Log.d("1111","1111111 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
						increaseLevel();
					} else if (am.getStreamVolume(AudioManager.STREAM_VOICE_CALL) < currentVoiceCallVolume) {

						if (currentLevelL > minLevel && currentLevelR > minLevel) {
							am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)-1, AudioManager.FLAG_SHOW_UI);
							currentVoiceCallVolume = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
							launchDialogActivity(DialogActivity.DECREASE);

							decreaseLevel();

						}
					}
				}
			}

			if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
				if (am.getStreamVolume(AudioManager.STREAM_RING) != currentRingVolume){
					//if (newVolume > 0){

					if (am.getStreamVolume(AudioManager.STREAM_RING) >= currentRingVolume) {

						am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING)-1, AudioManager.FLAG_SHOW_UI);
						currentRingVolume = am.getStreamVolume(AudioManager.STREAM_RING);
						launchDialogActivity(DialogActivity.INCREASE);
						//Log.d("1111","1111111 : " + currentMusicVolume + " " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
						increaseLevel();
					} else if (am.getStreamVolume(AudioManager.STREAM_RING) < currentRingVolume) {

						if (currentLevelL > minLevel && currentLevelR > minLevel) {
							am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING)-1, AudioManager.FLAG_SHOW_UI);
							currentRingVolume = am.getStreamVolume(AudioManager.STREAM_RING);
							launchDialogActivity(DialogActivity.DECREASE);

							decreaseLevel();

						}
					}
				}
			}

		}

	}
}
