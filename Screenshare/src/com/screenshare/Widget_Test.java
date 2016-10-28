package com.screenshare;

import java.io.File;
import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget_Test extends AppWidgetProvider{
	public static RemoteViews remoteViews;
	public String ACTION_WIDGET_RECEIVER ="ActionReceiverWidget";
    static final ComponentName THIS_APPWIDGET = new ComponentName("com.screenshare","com.screenshare.Widget_Test");
	public static Ipc ipc=null;
	public static int runState = 0;
	//0 = stopped
	//1 = stopped
	//2 = wait, state changing
	//3 = state should refresh 
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {        
	    
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);				

        Intent intent = new Intent(context, Widget_Test.class); 
        intent.setAction(ACTION_WIDGET_RECEIVER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.img, pendingIntent);
        
        if(runState != 2){
        	if(ipc==null)
        		ipc=new Ipc(context);
        	
        	if(ipc.runTest()){
        		if(runState!=1){
        			remoteViews.setImageViewResource(R.id.img, R.drawable.icon);
        			runState=1;
        			ipc.notiyShow(context,"Running");
        			
        		}
        	}else{
        		if(runState!=0){
        			remoteViews.setImageViewResource(R.id.img, R.drawable.icon_gray);
        			runState=0;
                    ipc.notiyDestroy(context);		            		            
        		}
        	}
        	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
        
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(ipc==null)
			ipc=new Ipc(context);
    	
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
		}else if(intent.getAction().equals(ACTION_WIDGET_RECEIVER) && runState!=2) {
	        if(ipc.runTest()){
	        	ipc.comBinAuth("stop");	    
	        }else{
	        	startservice();
	        }
	        
		if (remoteViews == null)
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);				
        	remoteViews.setImageViewResource(R.id.img, R.drawable.icon_c);
        	runState=2;	        
	        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
	        gm.updateAppWidget(THIS_APPWIDGET, remoteViews);
	        new StateTimer(context).start();
		}
		super.onReceive(context, intent);
	}
	
	@Override
	public void onEnabled(Context context){
		if(ipc==null)
    		ipc=new Ipc(context);
    	
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);				
	       ipc = new Ipc(context);
	        if(ipc.runTest()){	        
	        	runState=0;
	        }else{
	        	runState=1;
	        }
	}
	
	void startservice(){
		ipc.startService();
	}
	

class StateTimer extends Thread {
	Context tContext;
	public StateTimer(Context context) {
		tContext = context;

	}

	public void run() {
		try {
			Thread.sleep(5000);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(Widget_Test.runState==2)
			Widget_Test.runState=3;
		if(Widget_Test.ipc.runTest()){
			Widget_Test.remoteViews.setImageViewResource(R.id.img, R.drawable.icon);
			ipc.notiyShow(tContext,"Running");
		}else{
			Widget_Test.remoteViews.setImageViewResource(R.id.img, R.drawable.icon_gray);
            ipc.notiyDestroy(tContext);		            		            

			
		}
        AppWidgetManager gm = AppWidgetManager.getInstance(tContext);
        gm.updateAppWidget(THIS_APPWIDGET, Widget_Test.remoteViews);
	} 
} 

}
