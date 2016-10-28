package com.screenshare;

import com.screenshare.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;


public class Screenshare extends TabActivity {
	TabHost tabHost;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    Resources res = getResources(); 
	    tabHost = getTabHost();
	    TabHost.TabSpec spec; 
	    Intent intent; 

	    intent = new Intent().setClass(this, com.screenshare.activity.ScreenshareTab.class);
	    spec = tabHost.newTabSpec("control").setIndicator("Access Screen",
	                      res.getDrawable(R.drawable.ic_tab_control))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, com.screenshare.activity.SettingsTab.class);
	    spec = tabHost.newTabSpec("settings").setIndicator("Settings",
	                      res.getDrawable(R.drawable.ic_tab_settings))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
            intent = new Intent().setClass(this, com.screenshare.remotescreen.ScreenActivities.class);
            spec = tabHost.newTabSpec("remotescreen").setIndicator("Remote Screen",res.getDrawable(R.drawable.ic_tab_remote))
                                .setContent(intent);
            tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
	@Override
	public void onResume(){		
		super.onResume();
		String isChatMsg=null;
        Intent intent = getIntent();
        isChatMsg = intent.getAction();
        if(isChatMsg != null && isChatMsg.equals("chat")){
        	tabHost.setCurrentTab(2);
        }
	}		
}
