package com.screenshare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Chat_Test extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Notification notification;
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
		notification = new Notification();
		notification.icon = R.drawable.chat;
		notification.tickerText = "New chat message";				
		notification.when = System.currentTimeMillis();
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		Intent notifyIntent = new Intent(context,Screenshare.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyIntent.setAction("chat");	
		notification.setLatestEventInfo(context, "Screenshare", "New message", PendingIntent.getActivity(context, 0, notifyIntent, 0));
		
		manager.notify(1, notification);
		
	}
}
