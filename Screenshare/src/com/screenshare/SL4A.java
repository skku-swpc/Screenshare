
package com.screenshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class SL4A extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if ("screenshare.intent.action.SL4A.START".equalsIgnoreCase(action)){
				context.stopService(new Intent(context, ScriptService.class));
				context.startService(new Intent(context, ScriptService.class));
		        Log.i("SL4A", "service started");
//			}
		}
	}

}
