package com.screenshare;
import android.content.BroadcastReceiver;  
import android.content.ComponentName;
import android.content.Context;  
import android.content.Intent;  
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;  
import com.screenshare.Ipc;


public class BootRinstall extends BroadcastReceiver{
	public static Ipc ipc;
    @Override  
    public void onReceive(Context context, Intent intent) {  
    	ComponentName component=new ComponentName(context, NetTest.class);

		context.getPackageManager()
		    .setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		                                PackageManager.DONT_KILL_APP);
		
    	ipc = new Ipc(context);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean ASPref = prefs.getBoolean("autostart", true);
        boolean StatusBarPref = prefs.getBoolean("statusbar", true);
        
        String ddUserName = prefs.getString("ddusername", "empty");
        String ddDomain = prefs.getString("dddomain", "empty");
        String ddPassword = prefs.getString("ddpassword", "empty");
        boolean ddusing = prefs.getBoolean("ddusing", false);
        
        
	int version = -1;
	try {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = manager.getPackageInfo(context.getPackageName(),0);
		version = info.versionCode;
	} catch (Exception e) {	}

        if(ASPref && (version == -1 || version > 306)){
        	ipc.startService();

        	if(StatusBarPref || ddusing){
        		for (int i=0; i < 20 && !ipc.runTest(); i++){
        			try{
        				Thread.sleep(500);
			    	}catch(InterruptedException e){ }
        		}

       		
        		if(ipc.runTest()){
        			if(StatusBarPref){
        				ipc.notiyShow(context,"Screenshare service is running");
        			}
	    			if(ddusing){
	    				String hash = ddUserName+":"+ddPassword;
	    				hash = Base.encodeBytes(hash.getBytes());
	    			}
        		}
        	}
        	
        }

	}
    
}
