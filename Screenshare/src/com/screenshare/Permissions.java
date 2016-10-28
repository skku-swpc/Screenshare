package com.screenshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Permissions extends Activity {
	Ipc ipc;
	Context mContext;
	String password=null;
	String userName=null;
	int permission;
	int MODE=0;
	int MODE_BROWSER=0;
	int MODE_NEW_USER=1;
	int MODE_EDIT_OLDUSER=2;
	
	int[] checkBoxPerms = new int[8];
	int[] helpButtons = new int[8];
	int[] helpTxt = new int[8];
	int[] helpTitle = new int[8];

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		    //No call for super(). Bug on API Level > 11.
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preparepermission);
		mContext = getApplicationContext();
				
		checkBoxPerms[0] = R.id.checkbox_all;
		checkBoxPerms[1] = R.id.checkbox_screenshot;
		checkBoxPerms[2] = R.id.checkbox_gps;
		checkBoxPerms[3] = R.id.checkbox_chat;
		checkBoxPerms[4] = R.id.checkbox_public;
		checkBoxPerms[5] = R.id.checkbox_sms_contact;
		checkBoxPerms[6] = R.id.checkbox_files;
		checkBoxPerms[7] = R.id.checkbox_sdcard;
		
		helpButtons[0] = R.id.help_all;
		helpButtons[1] = R.id.help_screenshot;
		helpButtons[2] = R.id.help_gps;
		helpButtons[3] = R.id.help_chat;
		helpButtons[4] = R.id.help_public;
		helpButtons[5] = R.id.help_sms_contact;
		helpButtons[6] = R.id.help_files;
		helpButtons[7] = R.id.help_sdcard;

		helpTxt[0]=R.string.permission_help_all;
		helpTxt[1]=R.string.permission_help_sceenshot;
		helpTxt[2]=R.string.permission_help_gps;
		helpTxt[3]=R.string.permission_help_chat;
		helpTxt[4]=R.string.permission_help_public;
		helpTxt[5]=R.string.permission_help_sms_contact;
		helpTxt[6]=R.string.permission_help_files;
		helpTxt[7]=R.string.permission_help_sdcard;

		helpTitle[0]=R.string.permission_checkbox_all;
		helpTitle[1]=R.string.permission_checkbox_screenshot;
		helpTitle[2]=R.string.permission_checkbox_location;
		helpTitle[3]=R.string.permission_checkbox_chat;
		helpTitle[4]=R.string.permission_checkbox_public;
		helpTitle[5]=R.string.permission_checkbox_contact;
		helpTitle[6]=R.string.permission_checkbox_files;
		helpTitle[7]=R.string.permission_checkbox_sdcard;

		ipc=new Ipc(mContext);
		
		
	    View.OnClickListener helpclickListener = new OnClickListener() {
	        public void onClick(View v) {
	        	int id = v.getId();
	        	for(int i = helpButtons.length-1; i>=0; i--){
	        		if(helpButtons[i]==id){
	        			openHelpDialog(i);
	        		}
	        	}
	        		
	        }
	    };
	    		
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		for(int i = helpButtons.length-1; i>=0; i--){
			findViewById(helpButtons[i]).setOnClickListener(helpclickListener);
		}
		
		Intent intent = getIntent();
		userName = intent.getStringExtra("name");
		permission = intent.getIntExtra("perm", -2);
		password = intent.getStringExtra("password");
		
		final CheckBox cb = (CheckBox) findViewById(R.id.checkbox_all);
		cb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox checkBox;
				Boolean j = cb.isChecked();				
				for(int i = checkBoxPerms.length-1; i>0; i--){
					checkBox = (CheckBox) findViewById(checkBoxPerms[i]);
					checkBox.setChecked(j);
					checkBox.setEnabled(!j);
				}
				
			}
			
		});
		
		Button accept_btn = (Button)findViewById(R.id.permission_accept_button);
	    accept_btn.setOnClickListener(new OnClickListener() {  
			public void onClick(View v) {
				int perm = 0;
				boolean server=((CompoundButton) findViewById(R.id.checkbox_all)).isChecked();
				if(server){
					perm = -1;        			
				}else{
					int step=1;
					for(int i=1; i<checkBoxPerms.length; i++){        		
						perm += ((CompoundButton) findViewById(checkBoxPerms[i])).isChecked()?step:0;
						step*=2;
					}	    			
				}

				if(MODE_BROWSER==MODE){
					newUser(userName, password);
					setPermission(userName,password, perm);
				}else if(MODE_EDIT_OLDUSER==MODE){
						setPermission(userName, password, perm);        			
				}else{
					newUser(userName, password);
					setPermission(userName, password, perm);
				}
				finish();
				}
	    });
	    
	    Button dismiss_btn = (Button)findViewById(R.id.permission_dismiss_button);
	    dismiss_btn.setOnClickListener(new OnClickListener() {  
			public void onClick(View v) {
				
        		finish();

			}  
	    });
	    	    

	}

	@Override
	public void onResume(){		
		super.onResume();
		if(userName!=null && permission != -2 && password!=null){
			MODE=MODE_NEW_USER;
		}else if(password==null && permission != -2){
			MODE=MODE_EDIT_OLDUSER;
		}else{
			MODE=MODE_BROWSER;
		}
			
		if(MODE==MODE_BROWSER){
			
			ipc.notiyShow(mContext, "Running");
			
			userName = ipc.comBinAuth("getreg");
			
			setBoxes(parsePermission(-1));			
			String sIPAddress=null;
			if(userName !=null && !userName.equals("\n\n1384369580")){
				String userPass[] = userName.split("\n");
				userName = userPass[0];
				password = userPass[1];
				if(!"0".equals(userPass[2])){
					long ipAddress = Long.parseLong(userPass[2]);
					sIPAddress = Long.toString((ipAddress>>24)&255);
					sIPAddress += "."+Long.toString((ipAddress>>16)&255);
					sIPAddress += "."+Long.toString((ipAddress>>8)&255);
					sIPAddress += "."+Long.toString(ipAddress&255);
				}
			}else{
				finish();
			}
			
			if(sIPAddress!=null){
				((TextView) findViewById(R.id.register_username)).setGravity(Gravity.LEFT);
				((TextView) findViewById(R.id.register_username)).setText(Html.fromHtml("<b>Username: "+userName+  "</b><br />" + 
						"<small>IP: " +sIPAddress + "</small>"));
			}else{
				((TextView) findViewById(R.id.register_username)).setGravity(Gravity.LEFT);
				((TextView) findViewById(R.id.register_username)).setText(Html.fromHtml("<b>"+getString(R.string.manage_username_text)+userName+"</b>"));
			}

		}else if(MODE_NEW_USER==MODE){
			setBoxes(parsePermission(permission));
			((TextView) findViewById(R.id.register_username)).setGravity(Gravity.LEFT);
			((TextView) findViewById(R.id.register_username)).setText(Html.fromHtml("<b>"+getString(R.string.manage_username_text)+userName+"</b>"));
		}else{						
			setBoxes(parsePermission(permission));
			((TextView) findViewById(R.id.register_username)).setGravity(Gravity.LEFT);
			((TextView) findViewById(R.id.register_username)).setText(Html.fromHtml("<b>"+getString(R.string.manage_username_text)+userName+"</b>"));
		}		
	}
	
	
	private void openHelpDialog(int i) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int whichButton) {
	    		 
	    	 }
	     });
		alert.setIcon(R.drawable.icon);
		alert.setTitle(helpTitle[i]);
		alert.setMessage(getString(helpTxt[i]));		
		alert.show();
	}
	
	private String parsePermission(int perm){
		if(perm==-1){
			return "-1";
		}else{
			return Integer.toBinaryString(perm);			
		}
		
	}
	
	private void setBoxes(String perm){
		CheckBox checkBox;		
		if(perm.equals("-1")){			
			checkBox = (CheckBox) findViewById(R.id.checkbox_all);
			checkBox.setChecked(true);
			for(int i = checkBoxPerms.length-1; i>0; i--){
				checkBox = (CheckBox) findViewById(checkBoxPerms[i]);
				checkBox.setChecked(true);
				checkBox.setEnabled(false);
			}			
		}else{
			checkBox=(CheckBox) findViewById(checkBoxPerms[0]);
			checkBox.setChecked(false);
			int diff=perm.length();
			for(int i = perm.length(); i>0; i--){
				char c = perm.charAt(i-1);		;
				int itf=Character.digit(c, 10);
				boolean tf = (itf==1)?true:false;				
				checkBox=(CheckBox) findViewById(checkBoxPerms[diff-i+1]);
				checkBox.setChecked(tf);
			}

		}
		
	}
	
	private void newUser(String username, String passwd){
		ipc.comBinAuth("setpassword",username+"\n"+passwd);
	}	
	
	private void setPermission(String username, String passwd, int permission){
		ipc.comBinAuth("setpermission",username+"\n"+passwd+"\n"+permission);
		
	}
	
	
}
