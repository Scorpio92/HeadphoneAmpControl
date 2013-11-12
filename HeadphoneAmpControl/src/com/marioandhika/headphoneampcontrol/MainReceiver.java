package com.marioandhika.headphoneampcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainReceiver extends BroadcastReceiver {

	public static final int UNPLUGGED = 0;
	public static final int PLUGGED = 1;
	public static final int UNALTERED = 2;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean isPlugged;
		if (intent.getIntExtra("state", UNPLUGGED) == 0) {
			isPlugged = false;
		} else {
			isPlugged = true;
		}

		Intent service = new Intent(context, MainService.class);
		
		if (isPlugged) {
			//startservice
			service.putExtra(MainService.HEADSET_STATUS, PLUGGED);
			context.startService(service);
		} else {
			service.putExtra(MainService.HEADSET_STATUS, UNPLUGGED);
			context.startService(service);
		}
	}

}
