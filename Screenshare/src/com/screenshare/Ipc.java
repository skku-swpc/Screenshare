package com.screenshare;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.apache.http.params.HttpParams; 
import org.apache.http.params.BasicHttpParams; 
import org.apache.http.params.HttpConnectionParams;

public class Ipc {
	final static String TAG = "Screenshare_java";
	public static NotificationManager manager;
	public String port;
	public String sslport;
	public String random;
	private String authKey=null;
	private String workDir;
	private Context _context;

	public Ipc(Context context){
		_context = context;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		port = prefs.getString("port", "80");
		sslport = prefs.getString("sslport", "443");
		
		random = prefs.getString("random", "");
		if (random.length() == 0)
		{
			Random rnd = new Random();
			for (int i = 0; i < 16; i++)
			{
				int c = rnd.nextInt(62);
				if (c < 10)
					random += (char)(c+48);
				else if (c < 36)
					random += (char)(c+65-10);
				else
					random += (char)(c+97-36);
			}
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString("random",random);
			ed.commit();
		}
		workDir=context.getFilesDir().getPath()+"/";
		readAuthKey();
	}
	
	public String comBin_old(String url){
		InputStream is = null;
		try{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
			port = prefs.getString("port", "80");
			HttpGet httpGet = new HttpGet("http://127.0.0.1:"+port+"/"+url);
HttpParams httpParameters = new BasicHttpParams();
int timeoutConnection = 500;
HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
int timeoutSocket = 1000;
HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpclient.execute(httpGet);
			is = response.getEntity().getContent();
			StringBuffer b = new StringBuffer();
			int ch;
						
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			return s;
		}catch(Exception e){
			return null;
		}
	}
	
	public String comBinAuth(String url){
		InputStream is = null;
		readAuthKey();
		try{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
			port = prefs.getString("port", "80");
			HttpGet httpGet = new HttpGet("http://127.0.0.1:"+port+"/"+authKey+url);
HttpParams httpParameters = new BasicHttpParams();
int timeoutConnection = 500;
HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
int timeoutSocket = 1000;
HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpclient.execute(httpGet);
			is = response.getEntity().getContent();
			StringBuffer b = new StringBuffer();
			int ch;
						
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			if(s.equals("") || s.equals("\n"))	
				return null;
			else
				return s;			
		}catch(Exception e){
			return null;
		}
	}
	
	public void comBinAuth(String turl, String postData){
		readAuthKey();
		try{								
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
			port = prefs.getString("port", "80");
			Socket s = new Socket("127.0.0.1",Integer.parseInt(port));
		       
			DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
			byte[] utf = postData.getBytes("UTF-8");
			dataOutputStream.writeBytes("POST /"+authKey+turl +" HTTP/1.1\r\n"+
					"Host: 127.0.0.1\r\n"+
					"Connection: close\r\n"+
					"Content-Length: "+Integer.toString(utf.length)+"\r\n\r\n");
			dataOutputStream.write(utf,0,utf.length);
			dataOutputStream.flush();
			dataOutputStream.close();
			s.close();
		} catch (IOException e1) {
			
		}		
	}
	
	public String registerusername(String serveraddr,String uname,String versionName){
		InputStream is = null;
		try{
			HttpGet httpGet = new HttpGet("http://"+serveraddr+"/register_"+uname+"/"+random+"/"+versionName);
HttpParams httpParameters = new BasicHttpParams();
int timeoutConnection = 500;
HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
int timeoutSocket = 1000;
HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpclient.execute(httpGet);
			is = response.getEntity().getContent();
			StringBuffer b = new StringBuffer();
			int ch;
						
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			return s;
		}catch(Exception e){
			//e.printStackTrace();
			return "error";
		}
	}

	public boolean runTest(){
		String testString = comBin_old("javatest");	
		if ("Screenshare".equals(testString)){			
			return true;
		}else{
			return false;
		}
		
	}
	

	public void startService(){
		try {		
			String[] cmd = {"/data/data/com.screenshare/files/start.sh"};		
			File file = new File("/");
			Runtime.getRuntime().exec(cmd,null,file);
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		readAuthKey();
	}
	
	private void readAuthKey(){
		try {
		  FileInputStream fstream = new FileInputStream(workDir+"/authkey.txt");
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  authKey=br.readLine().replaceAll("/r","").replaceAll("/n","");
		  br.close();
		  in.close();
		  fstream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (Exception e) {
		}
	}
	
	public void notiyShow(Context pContext,String message){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
		if(!prefs.getBoolean("statusbar", true)){
			notiyDestroy(pContext);
			return;
		}
		Notification notification;		
	
		if(manager==null)
			manager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notification = new Notification();		
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.icon = R.drawable.icon;
		notification.when = System.currentTimeMillis();
		Intent notifyIntent = new Intent(pContext,Screenshare.class);
		notification.setLatestEventInfo(pContext, "Screenshare", message, PendingIntent.getActivity(pContext, 0, notifyIntent, 0));
		manager.notify(1, notification);
	}
		
	public void notifyUpdate(Context pContext){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
		if(prefs.getBoolean("statusbar", false))
			notiyShow(pContext,"");
		else
			notiyDestroy(pContext);
	}
	
	public void notiyDestroy(Context pContext){
		if(manager==null)
			manager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
		try{
			manager.cancel(1);				
		}catch(Exception e){}
	}

	public void sendMessage(String message){
		readAuthKey();
		try{

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		port = prefs.getString("port", "80");
	        Socket s = new Socket("127.0.0.1",Integer.parseInt(port));
		DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
		byte[] utf = message.getBytes("UTF-8");
		dataOutputStream.writeBytes("POST /"+authKey+"phonewritechatmessage HTTP/1.1\r\n"+
				"Host: 127.0.0.1\r\n"+
				"Connection: close\r\n"+
				"Content-Length: "+Integer.toString(utf.length)+"\r\n\r\n");
		dataOutputStream.write(utf,0,utf.length);
		dataOutputStream.flush();
		dataOutputStream.close();
	        s.close();
		} catch (IOException e1) {	
		}		
		
	}
	public InputSource getMessages(String ID) {
		readAuthKey();
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
			port = prefs.getString("port", "80");
			URL url = new URL("http://127.0.0.1:"+port+"/"+authKey+"phonegetchatmessage_"+ID);
			return new InputSource(url.openStream());
		} catch (Exception e) {
			return null;
		}	
	}

	public void kill() {
		String pid = null;
		try {
		  FileInputStream fstream = new FileInputStream(workDir+"/pid.txt");
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  pid=br.readLine();
		  br.close();
		  in.close();
		  fstream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();		
			return;
		} catch (IOException e) {
			e.printStackTrace();		
			return;
		}
		try {
			String cmd = "kill -9 " + pid;
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			return;

		} catch (IOException e) {
			e.printStackTrace();		
		} catch (InterruptedException e) {
			e.printStackTrace();		
		}
		for (String pre : new String[]{"/system/bin/","/system/xbin/","/system/xbin/bb/","/system/sbin/"})
		{
			try {
				String[] cmd = { pre+"kill", "-9", pid };
				File location = new File(pre);
				Process p = Runtime.getRuntime().exec(cmd, null, location);
				p.waitFor();
				return;

			} catch (IOException e) {
			e.printStackTrace();		
			} catch (InterruptedException e) {
			e.printStackTrace();		
			}
		}
	}
}
