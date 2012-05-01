package uk.test.dtouchSample1;

import android.content.Context;
import uk.ac.horizon.dtouchMobile.HIPreference;

public class PreferenceSample1 extends HIPreference {
	
	//branches default values
	protected int DEFAULT_MIN_BRANCHES = 2;
	protected int DEFAULT_MAX_BRANCHES = 5;
	protected int DEFAULT_MAX_EMPTY_BRANCHES = 1;
	protected int DEFAULT_VALIDATION_BRANCHES = 0;
	//leaves default values
	protected int DEFAULT_MAX_LEAVES = 10;
	protected int DEFAULT_VALIDATION_BRANCH_LEAVES = 0;
	protected int DEFAULT_CHECKSUM_MODULO = 1;
	
	public PreferenceSample1(Context context){
		super(context);
		this.setDefaultMinBranches(2);
		this.setDefaultMaxBranches(5);
		this.setDefaultEmptyBranches(1);
		this.setDefaultValidationBranches(0);
		this.setDefaultValidationBranchLeaves(0);
		this.setDefaultMaxLeaves(10);
		this.setDefaultChecksumModulo(1);
		this.setDefaultMarkerOccurrence(1);
	}
}
