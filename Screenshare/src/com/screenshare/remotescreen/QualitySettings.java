package com.screenshare.remotescreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.screenshare.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class QualitySettings extends PreferenceActivity implements OnSharedPreferenceChangeListener  {

	SharedPreferences prefs;
	Map <String,Boolean> booleanSettings = new HashMap<String, Boolean>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.remotescreen_quality);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
                
        booleanSettings.put("rscformatum",true);
        booleanSettings.put("rsclowres", true);
        //booleanSettings.put("rschalfsize", true);
        booleanSettings.put("rscflip", true);
        booleanSettings.put("rscorient",true);
        booleanSettings.put("rscframebuffer",true);
           
        for (Entry<String, Boolean> entry: booleanSettings.entrySet()) {
        	String key=entry.getKey();
        	boolean value=entry.getValue();
        	
        	getPreferenceManager().findPreference(key).setSelectable(value);        	
        }        
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//getPreferenceScreen().findPreference(key).setSelectable(tmp);
		//boolean tmpVal = sharedPreferences.getBoolean(key, true);
		//SharedPreferences.Editor ed = prefs.edit();
		//ed.putBoolean(key, tmpVal);
		//ed.commit();				

		/*
		Iterator<String> iterator = booleanSettings.keySet().iterator();
		while(iterator.hasNext()){
			String mapKey= (String) iterator.next();
			if(key.equals(mapKey)){
				Log.d("Screenshare_java | QualitySettings:",mapKey);	
				
				boolean tmp = sharedPreferences.getBoolean(mapKey,false);
				getPreferenceScreen().findPreference(mapKey).setSelectable(tmp);
				SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean(key, tmp);
				ed.commit();				
			}
		}
		*/
	}
}
