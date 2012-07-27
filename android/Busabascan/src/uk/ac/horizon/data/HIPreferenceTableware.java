package uk.ac.horizon.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import uk.ac.horizon.dtouchMobile.HIPreference;

public class HIPreferenceTableware extends HIPreference {
	
	private static String NO_OF_TILES = "no_of_tiles";
	private static final int DEFAULT_NO_OF_TILES = 2;   
	
	public HIPreferenceTableware(Context context){
		super(context);
		this.setDefaultMinBranches(4);
		this.setDefaultMaxBranches(4);
		this.setDefaultEmptyBranches(0);
		this.setDefaultValidationBranches(1);
		this.setDefaultValidationBranchLeaves(2);
		this.setDefaultMaxLeaves(6);
		this.setDefaultChecksumModulo(3);
		this.setDefaultMarkerOccurrence(2);
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
}
