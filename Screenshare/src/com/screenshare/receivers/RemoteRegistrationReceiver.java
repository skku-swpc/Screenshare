package com.screenshare.receivers;

import com.screenshare.Permissions;
import com.screenshare.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class RemoteRegistrationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("allowremotereg", true)){				
			Notification notification;
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);        
			notification = new Notification();
			notification.icon = R.drawable.icon_reg;
			notification.tickerText = context.getString(R.string.manage_newuser_ticker);				
			notification.when = System.currentTimeMillis();
			Intent notifyIntent = new Intent(context,Permissions.class);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notification.setLatestEventInfo(context, "Screenshare", context.getString(R.string.manage_newuser_ongoing), PendingIntent.getActivity(context, 0, notifyIntent, 0));
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			manager.notify(1, notification);
		}
	}

}
