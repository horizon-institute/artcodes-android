package uk.ac.horizon.dtouchMobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HIPreference {
	
	//branches default values
	protected static int DEFAULT_MIN_BRANCHES = 2;
	protected static int DEFAULT_MAX_BRANCHES = 5;
	protected static int DEFAULT_MAX_EMPTY_BRANCHES = 0;
	protected static int DEFAULT_VALIDATION_BRANCHES = 0;
	//leaves default values
	protected static int DEFAULT_MAX_LEAVES = 10;
	protected static int DEFAULT_VALIDATION_BRANCH_LEAVES = 0;
	protected static int DEFAULT_CHECKSUM_MODULO = 1;
	
	private static String MIN_BRANCHES = "min_branches";
	private static String MAX_BRANCHES = "max_branches";
	private static String EMPTY_BRANCHES = "empty_branches";
	private static String MAX_LEAVES = "max_leaves";
	private static String VALIDATION_BRANCHES = "validation_branches";
	private static String VALIDATION_BRANCH_LEAVES = "validation_branch_leaves";
	private static String CHECKSUM_MODULO = "checksum_modulo";
	private static String MEMBER_NAME = "member_name";
	
	private Context mContext;
	
	public HIPreference(Context context){
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
		if (sharedPrefs.contains(EMPTY_BRANCHES))
			value = Integer.parseInt(sharedPrefs.getString(EMPTY_BRANCHES, Integer.toString(DEFAULT_MAX_EMPTY_BRANCHES)));
		else
			value = DEFAULT_MAX_EMPTY_BRANCHES;
		return value;
	}
	
	public int getValidationBranches(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(VALIDATION_BRANCHES))
			value = Integer.parseInt(sharedPrefs.getString(VALIDATION_BRANCHES, Integer.toString(DEFAULT_VALIDATION_BRANCHES)));
		else
			value = DEFAULT_VALIDATION_BRANCHES;
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
		
	public int getValidationBranchLeaves(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(VALIDATION_BRANCH_LEAVES))
			value = Integer.parseInt(sharedPrefs.getString(VALIDATION_BRANCH_LEAVES, Integer.toString(DEFAULT_VALIDATION_BRANCH_LEAVES)));
		else
			value = DEFAULT_VALIDATION_BRANCH_LEAVES;
		return value;
	}
	
	public int getChecksumModulo(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(CHECKSUM_MODULO))
			value = Integer.parseInt(sharedPrefs.getString(CHECKSUM_MODULO, Integer.toString(DEFAULT_CHECKSUM_MODULO)));
		else
			value = DEFAULT_CHECKSUM_MODULO;
		return value;
	}
	
	public String getMemberName(){
		String value = "";
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MEMBER_NAME))
			value = sharedPrefs.getString(MEMBER_NAME, "");
		return value;
	}
	
	public void setDefaultMinBranches(int minBranches){
		
	}
	
	public void setDefaultMaxBranches(int maxBranches){
		
	}
	
	public void setDefaultEmptyBranches(int emptyBranches){
		
	}
	
	public void setDefaultValidationBranches(int validationBranches){
		
	}
	
	public void setDefaultMaxLeaves(int maxLeaves){
		
	}
	
	public void setDefaultChecksumModulo(int checksumModulo){
		
	}
}
