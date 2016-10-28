package com.screenshare.activity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Thread;

import com.screenshare.BinIO;
import com.screenshare.Ipc;
import com.screenshare.ManageUsers;
import com.screenshare.R;

public class ScreenshareTab extends Activity implements OnClickListener{

	final String TAG = "Screenshare_java";
	public SharedPreferences prefs;
	int NEW_VERSION;
	Ipc ipc;
	Context mContext;
	
	TextView ipText;
	
	boolean notifyBarPref = false;

	private String serverUsername=null;
	
	public static boolean waitProgress=false;
	public static ProgressDialog dialog = null;
	boolean upgrade_failed = false;

	private static final int MSG_LOADING_DISMISS = 0;
	private static final int MSG_LOADING_SHOW = 1;
	private static final int MSG_REFRESH = 2;
	private static final int MSG_UPGRADE_FAILED = 3;
	private static final int MSG_ROOT_FAILED = 4;
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenshare);
	    mContext = getApplicationContext();	    
	    BinIO binIO = new BinIO(mContext);
	    ipc = new Ipc(mContext);
	    
        NEW_VERSION = binIO.getCurrentVersionCode();
        int old_ver = binIO.oldVersion();
        dialog = new ProgressDialog(ScreenshareTab.this);                
        
        findViewById(R.id.startbutton).setOnClickListener(this);
        findViewById(R.id.stopbutton).setOnClickListener(this);       
        findViewById(R.id.helpbutton).setOnClickListener(this);

		findViewById(R.id.logbutton).setOnClickListener(this);
		
        if((NEW_VERSION != old_ver || old_ver == -1) && waitProgress==false){
        	waitProgress=true;
        	Toast t = Toast.makeText(mContext, R.string.main_upgradingwait, Toast.LENGTH_LONG);
        	t.show();
        	showProgressDialog(true);
        	new UpgradeThread(binIO).start();   		   
	    }
	}
	
	
	@Override
	public void onResume(){		
		super.onResume();
		if(waitProgress){
			showProgressDialog(false);
		}

		ipText = (TextView) findViewById(R.id.ipaddress);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
		serverUsername = prefs.getString("username", "");
		notifyBarPref = prefs.getBoolean("statusbar", true);                   
        refresh();                
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	if(waitProgress){
    		try{
    			dialog.dismiss();
    		}catch (Exception e) {
			}
    	}

    }
    

	public void onClick(View v) {
        switch (v.getId()) {        
        	case R.id.startbutton:        		
        		try{
        			showProgressDialog(false);
        		}catch (Exception e){}
        		
				ipc.startService();
                (new Thread(){
                   	public void run(){            		
	    				for (int i=0; i < 30 && !ipc.runTest(); i++){
	    				    try{
	    				    	   Thread.sleep(500);
	    				    	}catch(InterruptedException e){ }
	    				}
    				    try{
   				    	   Thread.sleep(500);
   				    	}catch(InterruptedException e){}
	    			    mHandler.sendEmptyMessage(MSG_LOADING_DISMISS);
	    			    mHandler.sendEmptyMessage(MSG_REFRESH);	    			    
	    			    if(!ipc.runTest() && !new BinIO(mContext).rootCheck()){
	    			    	mHandler.sendEmptyMessage(MSG_ROOT_FAILED);	    	
	    			    }
                    }
                }).start();            		
                break;
                
        	case R.id.stopbutton:
        		mHandler.sendEmptyMessage(MSG_LOADING_SHOW);
                (new Thread(){
                   	public void run(){		    	
   				    	ipc.comBinAuth("stop");
   				    	mHandler.sendEmptyMessage(MSG_LOADING_SHOW);
	    				for (int i=0; i < 10 && ipc.runTest(); i++){
	    				    try{
	    				    	   Thread.sleep(500);
	    				    	}catch(InterruptedException e){ }
	    				}
						ipc.kill();
  				    	
  				    	mHandler.sendEmptyMessage(MSG_REFRESH);
	    				mHandler.sendEmptyMessage(MSG_LOADING_DISMISS);	    			    
                    }
                }).start();  				
        	    break;
        	    
        	case R.id.helpbutton:						    			
    			showDialog(getString(R.string.main_help_txt)+getString(R.string.main_translators),getString(R.string.main_help));  
    			break;
        	                
        	case R.id.logbutton:
        		Intent logIntent = new Intent(getBaseContext(), Logs.class);
		        startActivity(logIntent);
        		break;
        		
        	case R.id.usersettingsbtn:
        		Intent UsersActivity = new Intent(getBaseContext(), ManageUsers.class);
                startActivity(UsersActivity);        		
        		break;
        }
		
	}
	
    Handler mHandler = new Handler() {
    	public void handleMessage(final Message msg) {
            switch (msg.what) {
            	case MSG_LOADING_SHOW:
            		waitProgress=true;
            		showProgressDialog(false);
            		break;
            	case MSG_LOADING_DISMISS:
            		try{
            			if(dialog.isShowing())            		
            				dialog.dismiss();
            		}catch (Exception e) {}
            		waitProgress=false;
            		break;            		
            	case MSG_REFRESH:
            		refresh();
            		try{
            			dialog.dismiss();
            		}catch (Exception e) {
					}            		
            		waitProgress=false;
            		break;
            	case MSG_UPGRADE_FAILED:
            		showDialog(getString(R.string.main_upgradfailed), getString(R.string.warning));
            		break;
            	case MSG_ROOT_FAILED:
            		showDialog(getString(R.string.main_rootfailed), getString(R.string.warning));
            		break;            		
            }
    	}
    };
    
	private void showProgressDialog(boolean upgrading){
		if(!dialog.isShowing()){
			dialog = new ProgressDialog(ScreenshareTab.this);
			dialog.setTitle("Screenshare");
			dialog.setIcon(R.drawable.icon);
			if (upgrading)
				dialog.setMessage(getString(R.string.main_upgradingwait));
			else
				dialog.setMessage(getString(R.string.main_progress));
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);			 
			dialog.show();
		}
	}
     
		public void refresh(){				
			    if (ipc.runTest() == false){
				    String iptext = "";
				    iptext += getString(R.string.main_servicenotrunning); 
				    ipText.setText(iptext);				    
				    findViewById(R.id.stopbutton).setEnabled(false);
				    findViewById(R.id.startbutton).setEnabled(true);
					ipc.notiyDestroy(mContext);
			    }else{
			    	String iptext = "";
			    	iptext += getString(R.string.main_phoneavailableat);
			    	ArrayList<String> ipCim = getLocalIpAddress();
			    	if (ipCim == null){			    		
			    		iptext += getString(R.string.main_netnotexist); 
			    	}else{
			    		for (String e : ipCim)
					{
						if (e.length() == 0 || e.contains(":"))
							continue; // we haven't prepared for IPv6 from the C++ side
			    			if (ipc.port.equals("80"))
			    				iptext += "http://"+e;
			    			else
			    				iptext += "http://"+e+":"+ipc.port;			    			
			    			iptext+=("\n");
			    			if (ipc.sslport.equals("443"))
			    				iptext += "https://"+e;
			    			else
			    				iptext += "https://"+e+":"+ipc.sslport;
			    			iptext+=("\n");
			    		}
			    	}
			    	
			    	if (prefs.getBoolean("server",false) && serverUsername.length() > 0)
			    		iptext += "http://"+this.getString(R.string.main_server)+this.getString(R.string.main_serverport)+"/"+serverUsername;
			    	else
			    		iptext += this.getString(R.string.main_serverreason)+" "+this.getString(R.string.main_server); 

			    	ipText.setText(iptext);
			    	
				    findViewById(R.id.stopbutton).setEnabled(true);
					findViewById(R.id.startbutton).setEnabled(false);					

					if(notifyBarPref){
						ipc.notiyShow(mContext, "Running");
					}
			    }				
		}
		
	
	
	private String readNetTestInfo() {
			String dir = mContext.getFilesDir().getPath();
			File file = new File(dir+"/nettest.txt");
			if(!file.exists())
				return null;
			
			FileInputStream fstream;
			try {
				fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine = br.readLine();
				fstream.close();				
				return strLine;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
					
		}


	@Override
	public void onDestroy(){
		super.onDestroy();
	}
		
	public ArrayList<String> getLocalIpAddress() {
		ArrayList<String> ipList = new ArrayList<String>(); 
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    ipList.add(inetAddress.getHostAddress().toString());
	                }	                
	            }
	        }
	        
	        return ipList;
	    } catch (Exception ex) {
	    }
	    return null;
	}
		
	private void showDialog(String message, String title) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int whichButton) {
	    		 
	    	 }
	     });
		alert.setIcon(R.drawable.icon);
		alert.setTitle(title);
		alert.setMessage(message);		
		alert.show();
	}
		 
	 
	 public class UpgradeThread extends Thread{
			BinIO binIO = null;
	    	public UpgradeThread(BinIO binIO) {
	    		this.binIO=binIO;
			}
	    	
			public void run(){
			boolean runPreviously = false;
			if (ipc.runTest())
				runPreviously = true;
	    		ipc.comBin_old("stop");
	    		ipc.comBinAuth("stop");
			try{
				Thread.sleep(500);
			}catch(InterruptedException e){ }
			upgrade_failed = false;
//			if (ipc.runTest())
				ipc.kill();
	    		
			    String path = mContext.getFilesDir().getPath();
			    
			    boolean screenshareupgrade = false;

				screenshareupgrade = binIO.unpackSingleFile("bin", "screenshare", path);
			    	Log.d(TAG,"unpack screenshare");   
		    	
			    binIO.delDir(new File(path+"/plugins"));
			    binIO.delDir(new File(path+"/client"));
				if(screenshareupgrade && binIO.openAssets("screenshare", "", path) &&
					binIO.chmod775("screenshare") &&
					binIO.chmod775("sqlite3") &&
					binIO.chmod775("openssl") &&
					binIO.chmod775("start.sh"))
				{
					binIO.versionWrite(NEW_VERSION);
				}else{
					mHandler.sendEmptyMessage(MSG_UPGRADE_FAILED);
					upgrade_failed = true;
				}
				
				if(!binIO.rootCheck()){
					mHandler.sendEmptyMessage(MSG_ROOT_FAILED);
				}
				else
				{
					if (upgrade_failed == false && runPreviously == true)
						ipc.startService();

				}
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e){ }
				mHandler.post(new Runnable() {
			        public void run() {	
			        	try{
			        		dialog.dismiss();
			        		refresh();
			        	}catch(Exception e){
			        		
			        	}
			        }
			    });
				waitProgress=false;
	    	}
	    }
}
