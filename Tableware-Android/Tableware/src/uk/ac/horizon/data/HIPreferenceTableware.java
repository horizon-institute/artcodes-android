package uk.ac.horizon.data;

import android.content.Context;
import uk.ac.horizon.dtouchMobile.HIPreference;

public class HIPreferenceTableware extends HIPreference {
	
	//branches default values
	protected static int DEFAULT_MIN_BRANCHES = 8;
	protected static int DEFAULT_MAX_BRANCHES = 8;
	protected static int DEFAULT_MAX_EMPTY_BRANCHES = 0;
	protected static int DEFAULT_VALIDATION_BRANCHES = 4;
	//leaves default values
	protected static int DEFAULT_MAX_LEAVES = 10;
	protected static int DEFAULT_VALIDATION_BRANCH_LEAVES = 1;
	protected static int DEFAULT_CHECKSUM_MODULO = 10;
	
	public HIPreferenceTableware(Context context){
		super(context);
	}
}
