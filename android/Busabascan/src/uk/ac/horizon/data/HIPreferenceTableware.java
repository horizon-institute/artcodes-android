package uk.ac.horizon.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import uk.ac.horizon.dtouchMobile.HIPreference;

public class HIPreferenceTableware extends HIPreference {
	
	private static String NO_OF_TILES = "no_of_tiles";
	private static final int DEFAULT_NO_OF_TILES = 1;   
	private static String KITCHEN_IP = "kitchen_ip";
	private static final String DEFAULT_KITCHEN_IP = "192.168.0.8";   
	
	public HIPreferenceTableware(Context context){
		super(context);
	}
	
	public int getNumberOfTiles(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(NO_OF_TILES))
			value = Integer.parseInt(sharedPrefs.getString(NO_OF_TILES, Integer.toString(DEFAULT_NO_OF_TILES)));
		else
			value = DEFAULT_NO_OF_TILES;
		return value;
	}
	
	public String getKitchenIP(){
		String value = "";
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(KITCHEN_IP))
			value = sharedPrefs.getString(KITCHEN_IP, DEFAULT_KITCHEN_IP);
		else
			value = DEFAULT_KITCHEN_IP;
		return value;
	}
}
