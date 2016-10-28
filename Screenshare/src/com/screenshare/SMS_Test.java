package com.screenshare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.net.Uri;
import com.screenshare.Ipc;


public class SMS_Test extends BroadcastReceiver{
	public static Context mContext;
	Ipc ipc;

	@Override	
	public void onReceive(Context context, Intent intent) {
		
		mContext = context;
		ipc = new Ipc(mContext);
		String numbers = "";
		String message = "";
		FileInputStream content;
		String file=context.getFilesDir().getPath()+"/smsqueue";
		try {
			content = new FileInputStream(file);
			InputStreamReader input = new InputStreamReader(content);
			BufferedReader buffreader = new BufferedReader(input);
			
			String line = buffreader.readLine();
			numbers = line;

			message = buffreader.readLine();
			while (( line = buffreader.readLine()) != null) {
				message=message.concat("\n");
				message=message.concat(line);
			}

			String[] nums = numbers.split(",");
			for (int i = 0; i < nums.length; i++)
				writeSMS(nums[i], message);
			if (nums.length > 0)
			{
				ipc.comBinAuth("sendbroadcast");				
			}
			File dFile = new File(file);
			dFile.delete();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
    
    public void writeSMS(String num, String mess){
        ContentValues values = new ContentValues();
        values.put("address", num);
        values.put("body",  mess);
        values.put("status",64);
        values.put("read",Integer.valueOf(1));
        mContext.getContentResolver().insert(Uri.parse("content://sms/queued"), values);
    }

 }
