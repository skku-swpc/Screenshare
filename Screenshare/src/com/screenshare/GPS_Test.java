package com.screenshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import java.lang.Math;
import com.screenshare.Ipc;

public class GPS_Test extends BroadcastReceiver{
	private LocationManager locationManager;
	private Location location;
	static private mylocationlistener llGPS;
	static private mylocationlistener llnetwork;
	static private boolean started=false;
	public static Ipc ipc;
	@Override
	public void onReceive(Context context, Intent intent) {
			
		String action = intent.getAction();
		String intentResult = null;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if ("screenshare.intent.action.GPS.STOP".equalsIgnoreCase(action)){
				try{
					locationManager.removeUpdates(llGPS);
					locationManager.removeUpdates(llnetwork);
					llGPS = null;
					llnetwork = null;
				}catch(Exception e){}
				started = false;
			return;
		}    
		
		if (started == false)
		{
			ipc = new Ipc(context);
			llGPS = new mylocationlistener();
			Time newt;
			newt =  new Time();
			newt.setToNow();
			String s = newt.toString(); 
			try{
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llGPS);
				mylocationlistener llnetwork = new mylocationlistener();
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, llnetwork);
				started = true;
			}
			catch(Exception e){};
		}
		
		long unixTime = System.currentTimeMillis() / 1000L;
		long lUnixTime;
		
			intentResult="";
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location == null){
			}else{
				lUnixTime =unixTime-(location.getTime()/1000);
				intentResult+= "_nlat_"+Math.round(location.getLatitude()*10000000L)/10000000.0+"_nlong_"+Math.round(location.getLongitude()*10000000L)/10000000.0+"_nmtime_"+lUnixTime+"_naccu_";
				if (location.getAccuracy() == 0.0)
					intentResult += '-';
				else
					intentResult += (int)(location.getAccuracy())+"&nbsp;m";
			}
		
	   	    
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null){
			}else{
				lUnixTime =unixTime-(location.getTime()/1000);
				intentResult+="_glat_"+Math.round(location.getLatitude()*10000000L)/10000000.0+"_glong_"+Math.round(location.getLongitude()*10000000.0)/10000000.0+"_gmtime_"+lUnixTime+"_gaccu_";
				if (location.getAccuracy() == 0.0)
					intentResult += '-';
				else
					intentResult += (int)(location.getAccuracy())+"&nbsp;m";
			}
			
			ipc.comBinAuth("gpsset"+intentResult);
	}


    private class mylocationlistener implements LocationListener {

		public void onLocationChanged(Location location) {

		}

		public void onProviderDisabled(String provider) {

			
		}

		public void onProviderEnabled(String provider) {

			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

			
		}

        }	

}
