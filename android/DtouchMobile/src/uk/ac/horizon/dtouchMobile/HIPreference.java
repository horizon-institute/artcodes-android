package uk.ac.horizon.dtouchMobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
/**
 * This class defines the constraints for d-touch markers.It contains two sets of parameters. 
 * The first set defines the d-touch markers which needs to be identified. For example max & min
 * branches in a marker, empty branches and max leaves in a branch. 
 * The second set of parameters are used to define to validate a marker. It defines 
 * the number of validation branches, leaves in a validation branch and the checksum modulo.
 *    
 * @author pszsa1
 *
 */
public class HIPreference {
	
	//branches default values
	protected int DEFAULT_MIN_BRANCHES = 2;
	protected int DEFAULT_MAX_BRANCHES = 5;
	protected int DEFAULT_MAX_EMPTY_BRANCHES = 0;
	protected int DEFAULT_VALIDATION_BRANCHES = 0;
	//leaves default values
	protected int DEFAULT_MAX_LEAVES = 10;
	protected int DEFAULT_VALIDATION_BRANCH_LEAVES = 0;
	protected int DEFAULT_CHECKSUM_MODULO = 1;
	//marker occurrence value
	protected int DEFAULT_MARKER_OCCURRENCE = 1;
	
	//Minimum & Maximum number of branches.
	private static String MIN_BRANCHES = "min_branches";
	private static String MAX_BRANCHES = "max_branches";
	//Empty branches. A d-touch marker can not have all empty branches.
	private static String EMPTY_BRANCHES = "empty_branches";
	//Maximum leaves in a branch.
	private static String MAX_LEAVES = "max_leaves";
	//Maximum validation branches.
	private static String VALIDATION_BRANCHES = "validation_branches";
	//Maximum leaves in a validation branch.
	private static String VALIDATION_BRANCH_LEAVES = "validation_branch_leaves";
	//The total number of leaves in a marker should be divisible by the checksum modulo.
	private static String CHECKSUM_MODULO = "checksum_modulo";
	//Maximum number of marker occurrence.
	private static String MARKER_OCCURRENCE = "marker_occurrence"; 
	
	protected Context mContext;
	
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
	
	public int getMarkerOccurrence(){
		int value = -1;
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (sharedPrefs.contains(MARKER_OCCURRENCE))
			value = Integer.parseInt(sharedPrefs.getString(MARKER_OCCURRENCE, Integer.toString(this.DEFAULT_MARKER_OCCURRENCE)));
		else
			value = this.DEFAULT_MARKER_OCCURRENCE;
		return value;
	}
	
	public void setDefaultMinBranches(int minBranches){
		this.DEFAULT_MIN_BRANCHES = minBranches;
	}
	
	public void setDefaultMaxBranches(int maxBranches){
		this.DEFAULT_MAX_BRANCHES = maxBranches;
	}
	
	public void setDefaultEmptyBranches(int emptyBranches){
		this.DEFAULT_MAX_EMPTY_BRANCHES = emptyBranches;
	}
	
	public void setDefaultValidationBranches(int validationBranches){
		this.DEFAULT_VALIDATION_BRANCHES = validationBranches;
	}
	
	public void setDefaultValidationBranchLeaves(int validationBranchLeaves){
		this.DEFAULT_VALIDATION_BRANCH_LEAVES = validationBranchLeaves;
	}
	
	public void setDefaultMaxLeaves(int maxLeaves){
		this.DEFAULT_MAX_LEAVES = maxLeaves;
	}
	
	public void setDefaultChecksumModulo(int checksumModulo){
		this.DEFAULT_CHECKSUM_MODULO = checksumModulo;
	}
	
	public void setDefaultMarkerOccurrence(int markerOccurrence){
		this.DEFAULT_MARKER_OCCURRENCE = markerOccurrence;
	}
}
