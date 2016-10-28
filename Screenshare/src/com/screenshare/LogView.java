package com.screenshare;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

public class LogView extends Activity{
	TextView logText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.logview);
		logText = (TextView) findViewById(R.id.logcontent);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		//File logFile = new File("log.txt");;
        try {
            FileInputStream fis = new FileInputStream(this.getFilesDir().getPath()+"/log.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            String line=null;
            while ((line=dis.readLine())!=null) {            
            	logText.append(line+"\n");
            }
            fis.close();
        } catch (IOException e) {
        	showDialog(getString(R.string.logview_nologmsg),getString(R.string.warning));
        } catch (OutOfMemoryError e){
        	showDialog(getString(R.string.logview_toobigfile),getString(R.string.warning));
        }

	}
	
	private void showDialog(String message, String title) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);			
		alert.setNeutralButton("ok", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();				
			}
		}) ;		
		alert.setIcon(R.drawable.icon);
		alert.setTitle(title);
		alert.setMessage(message);		
		alert.show();
	}

}
