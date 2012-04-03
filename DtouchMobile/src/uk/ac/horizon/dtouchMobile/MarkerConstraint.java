package uk.ac.horizon.dtouchMobile;

import java.util.List;

public class MarkerConstraint {
	private List<Integer> markerCodes;
	private HIPreference mPreference;
	
	
	public MarkerConstraint(HIPreference preference, List<Integer> codes){
		this.mPreference = preference;
		this.markerCodes = codes;
	}
	
	public Boolean verifyMarkerCode(){
		Boolean valid = false;
		if (verifyValidationBranches())
			valid = verifyChecksum();
		return valid;
	}
	
	private boolean verifyValidationBranches(){
		boolean valid = false;
		int numberOfValidationBranches = 0;
		for (int code : markerCodes){
			if (code == mPreference.getValidationBranchLeaves()){
				numberOfValidationBranches++;
			}
		}
		if (numberOfValidationBranches >= mPreference.getValidationBranches())
			valid = true;
		return valid;
	}
	
	private boolean verifyChecksum(){
		boolean valid = false;
		int numberOfLeaves = 0;
		for (int code: markerCodes){
			numberOfLeaves += code;
		}
		if (mPreference.getChecksumModulo() > 0){
			double checksum = numberOfLeaves % mPreference.getChecksumModulo();
			if (checksum == 0){
				valid = true;
			}
		}
		return valid;
	}
	
}
