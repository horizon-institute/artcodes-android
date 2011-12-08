package uk.ac.horizon.tableware;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TWPreference {
		
	private static int DEFAULT_MIN_BRANCHES = 3;
	private static int DEFAULT_MAX_BRANCHES = 12;
	private static int DEFAULT_MAX_EMPTY_BRANCHES = 2;
	private static int DEFAULT_MAX_LEAVES = 20;
	
	private static String MIN_BRANCHES = "min_branches";
	private static String MAX_BRANCHES = "max_branches";
	private static String MAX_EMPTY_BRANCHES = "max_empty_branches";
	private static String MAX_LEAVES = "max_leaves";
	
	private Context mContext;
	
	public TWPreference(Context context){
		this.mContext = context;
	}
	
	public int getMinBranches(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MIN_BRANCHES))
			value = Integer.parseInt(sharedPrefs.getString(MIN_BRANCHES, Integer.toString(DEFAULT_MIN_BRANCHES)));
		else
			value = DEFAULT_MIN_BRANCHES;
		return value;
	}
	
	public int getMaxBranches(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MAX_BRANCHES))
			value = Integer.parseInt(sharedPrefs.getString(MAX_BRANCHES, Integer.toString(DEFAULT_MAX_BRANCHES)));
		else
			value = DEFAULT_MAX_BRANCHES;
		return value;
	}
	
	public int getMaxEmptyBranches(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MAX_EMPTY_BRANCHES))
			value = Integer.parseInt(sharedPrefs.getString(MAX_EMPTY_BRANCHES, Integer.toString(DEFAULT_MAX_EMPTY_BRANCHES)));
		else
			value = DEFAULT_MAX_EMPTY_BRANCHES;
		return value;
	}
	
	public int getMaxLeaves(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MAX_LEAVES))
			value = Integer.parseInt(sharedPrefs.getString(MAX_LEAVES, Integer.toString(DEFAULT_MAX_LEAVES)));
		else
			value = DEFAULT_MAX_LEAVES;
		return value;
	}
	
}
