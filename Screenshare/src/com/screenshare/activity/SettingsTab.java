package com.screenshare.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.screenshare.Base;
import com.screenshare.BinIO;
import com.screenshare.Ipc;
import com.screenshare.ManageUsers;
import com.screenshare.R;

public class SettingsTab extends Activity implements OnClickListener{

	final String TAG = "Screenshare_java";
	public SharedPreferences prefs;
	public SharedPreferences.Editor prefsEditor;

	Ipc ipc;
	Context mContext;
	
	boolean notifyBarPref = false;
	
	boolean ddusing = false;
	boolean ddrefresh = false;	
	private String ddUserName = "";
	private String ddDomain = "";
	private String ddPassword = "";	
	

	private String serverUsername=null;	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		    //No call for super(). Bug on API Level > 11.
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_tab);
	    mContext = getApplicationContext();	    
	    ipc = new Ipc(mContext);
	                  
        findViewById(R.id.helpbutton).setOnClickListener(this);
		findViewById(R.id.edit_portbutton).setOnClickListener(this);
		findViewById(R.id.autostartcheckbox).setOnClickListener(this);
		findViewById(R.id.notifisetting).setOnClickListener(this);
		findViewById(R.id.usersettingsbtn).setOnClickListener(this);
		findViewById(R.id.allow_remote_reg).setOnClickListener(this);
	}
	
	
	@Override
	public void onResume(){		
		super.onResume();
		
		//read the saved settings
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefsEditor = prefs.edit();
		
		//indicate the saved settings
		((CompoundButton) findViewById(R.id.autostartcheckbox)).setChecked(prefs.getBoolean("autostart", true));
		((CompoundButton) findViewById(R.id.notifisetting)).setChecked(prefs.getBoolean("statusbar", true));
		((CompoundButton) findViewById(R.id.allow_remote_reg)).setChecked(prefs.getBoolean("allowremotereg", true));
		 
		serverUsername = prefs.getString("username", "");
		notifyBarPref=prefs.getBoolean("statusbar", true);             
        refresh();                
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    }
    

	public void onClick(View v) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefsEditor = prefs.edit();
        switch (v.getId()) {
        	case R.id.helpbutton:						    			
    			showDialog(getString(R.string.main_help_txt)+getString(R.string.main_translators),getString(R.string.main_help));  
    			break;
    			
        		
        	case R.id.edit_portbutton:
        		showPortDialog();
    			break;
    		
 	            
        	case R.id.autostartcheckbox:
		        if (((CompoundButton) findViewById(R.id.autostartcheckbox)).isChecked()) {
		        	prefsEditor.putBoolean("autostart", true);	
		        }else{
		        	prefsEditor.putBoolean("autostart", false);		         
		        }
	        	prefsEditor.commit();
		        break;
		    
        	case R.id.allow_remote_reg:
        		boolean state = ((CompoundButton) findViewById(R.id.allow_remote_reg)).isChecked();
        		prefsEditor.putBoolean("allowremotereg", state);
        		prefsEditor.commit();
        		break;
        		
        	case R.id.notifisetting:
		        if (((CompoundButton) findViewById(R.id.notifisetting)).isChecked()) {
		        	notifyBarPref = true;
		        	prefsEditor.putBoolean("statusbar", true);
		        	prefsEditor.commit();
		        	if(ipc.runTest()){		    
		        	 ipc.notiyShow(mContext,"Running");
		        	}		        	
		        }else{
		        	notifyBarPref = false;
		        	prefsEditor.putBoolean("statusbar", false);
		        	prefsEditor.commit();
		            ipc.notiyDestroy(mContext);		            		            
		        }		        
        		break;
        		
        	case R.id.usersettingsbtn:
        		Intent UsersActivity = new Intent(getBaseContext(), ManageUsers.class);
                startActivity(UsersActivity);        		
        		break;
        }
		
	}
	
	public void refresh(){				
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefsEditor = prefs.edit();
		if (ipc.runTest() == false){
			findViewById(R.id.edit_portbutton).setEnabled(true);
			findViewById(R.id.usersettingsbtn).setEnabled(false);
			ipc.notiyDestroy(mContext);
			ddrefresh = true;			
		}else{			    			    				
			findViewById(R.id.edit_portbutton).setEnabled(false);
			findViewById(R.id.usersettingsbtn).setEnabled(true);
					
			if(notifyBarPref){
				ipc.notiyShow(mContext, "Running");
			}
					
			if(ddusing && ddrefresh){
				String hash = ddUserName+":"+ddPassword;
				hash = Base.encodeBytes(hash.getBytes()); 
				new DownloadFilesTask().execute("dyndns"+ddDomain+"&"+hash);
				prefsEditor.putBoolean("ddchange", false);
				prefsEditor.commit();
				ddrefresh = false;
			}
		}				
	}
		
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
		
	
	private void showPortDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.main_setport_dialog);		
		dialog.setTitle(getString(R.string.main_portWindowTitle));
		final EditText input = (EditText) dialog.findViewById(R.id.edit_port);
		input.setText(ipc.port);
		final EditText sslinput = (EditText) dialog.findViewById(R.id.edit_sslport);
		sslinput.setText(ipc.sslport);
		
   		View buttonOK = dialog.findViewById(R.id.dialog_ok);   		
//		Log.d("Screenshare_java: ","port dialog");
		buttonOK.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
        		final EditText input = (EditText) dialog.findViewById(R.id.edit_port);
        		final EditText sslinput = (EditText) dialog.findViewById(R.id.edit_sslport);
        		
        		String port = input.getText().toString();
	            String sslport = sslinput.getText().toString();
	            int valuePort = 0;
	            int valueSslPort = 0;
	            try{
	            	valuePort = Integer.parseInt( port );
	            	valueSslPort = Integer.parseInt( sslport );	            	
	            }catch (Exception e) {
	    			 showDialog(getString(R.string.main_invalidport),getString(R.string.warning)); 
				}	            
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefsEditor = prefs.edit();
	            
	    		 if(valuePort >0 && valuePort <=65535 && valueSslPort >0 && valueSslPort <= 65535 && valuePort != valueSslPort){
			    	 prefsEditor.putString("port",port);
			    	 prefsEditor.putString("sslport",sslport);
			    	 prefsEditor.commit();
			         ipc.port = port;
			         ipc.sslport = sslport;
			         refresh();
			         dialog.dismiss();
	    		 }else{
	    			 showDialog(getString(R.string.main_invalidport),getString(R.string.warning)); 
	    		 }		
			}
			
		});

		View buttonCancel = dialog.findViewById(R.id.dialog_cancel);		
		buttonCancel.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
   		dialog.show();
 	    dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
   		
	}
	
	private void showUsernameDialog() {		
	    AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(R.string.main_usernametitle);
	    alert.setMessage(R.string.main_usernamemessage);
	     final EditText input = new EditText(this);
	     input.setSingleLine(true);
	     input.setText(serverUsername);
	     input.setInputType(InputType.TYPE_CLASS_TEXT); 

	     alert.setView(input);
	     alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int whichButton) {  
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefsEditor = prefs.edit();
	            String un = input.getText().toString();
	            String t = un.toLowerCase();
	            Boolean success = true;
	            if (t.length() == 0){
	    			 showDialog(getString(R.string.main_emptyusername),getString(R.string.warning));
			         success = false;
            	}
	            for (int i = 0; i < t.length();i++){
	            	char c = t.charAt(i);
	            	if ((c < 'a' || c > 'z') && (c < '0' || c > '9')){
		    			 showDialog(getString(R.string.main_alphabeticallowd),getString(R.string.warning));
				         success = false;
		    			 break;
	            	}
	            		
	            }
	            
	            if (success){
	     	       mContext = getApplicationContext();
	    	       BinIO binIO = new BinIO(mContext);
	    	       String resp = ipc.registerusername(getString(R.string.main_server), un, binIO.getCurrentVersionName());
	            	if (resp.equals("OK") == false){
	            		if (resp.equals("error") == false)
	            			showDialog(getString(R.string.main_unableregisterserver)+"\n"+resp,getString(R.string.warning));
	            		else
	            			showDialog(getString(R.string.main_unableregisterserver),getString(R.string.warning));
				         success = false;
	            	}
	            	else
	            	{
	            		Toast toast = Toast.makeText(mContext, R.string.main_unameregistered, Toast.LENGTH_LONG);	            		
		    			toast.show();
	            	}
	            }
		         if (success)
		         {
		        	 prefsEditor.putString("username",un);
		        	 prefsEditor.putBoolean("server",true);
		        	 prefsEditor.commit();
			         serverUsername = un;
		         }
		         else
		         {
		         }

    			 ipc.comBinAuth("reread");
		         refresh();
		         return;
	           }  
	        });  

	     alert.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int which) {
	    		 String un = input.getText().toString();
	    		 if(un.equals("") || serverUsername.equals("")){
	    		 }
			     return;   
	    	 }
	     });
	   alert.show();
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
	
	
	 private class DownloadFilesTask extends AsyncTask<String, Void, String> {
		 
	     protected void onPostExecute(String message) {
	    	if(message != null && !message.equals("")){
	    			Toast t = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
	    			t.show();
	    	}
	     }

		@Override
		protected String doInBackground(String... params) {
			return ipc.comBinAuth(params[0]);			
		}

	 }

}
