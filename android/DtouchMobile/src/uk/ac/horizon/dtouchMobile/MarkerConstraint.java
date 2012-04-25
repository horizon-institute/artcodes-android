package uk.ac.horizon.dtouchMobile;

import java.util.List;
/**
 * This class is used to check the constraints defined by the preference class. 
 * @author pszsa1
 *
 */
public class MarkerConstraint {
	private List<Integer> markerCodes;
	private HIPreference mPreference;
	
	/**
	 * Constructor
	 * @param preference It holds the preference values for a d-touch marker.
	 * @param codes The code which is determined by the DetectMarker class.
	 */
	public MarkerConstraint(HIPreference preference, List<Integer> codes){
		this.mPreference = preference;
		this.markerCodes = codes;
	}
	
	/**
	 * This function checks if the code fulfils the constraints provided by the preferences.
	 * @return
	 */
	public Boolean verifyMarkerCode(){
		Boolean valid = false;
		if (verifyValidationBranches())
			valid = verifyChecksum();
		return valid;
	}
	
	/**
	 * It checks the number of validation branches as given in the preferences. The code is valid if the number of branches which contains the validation code are 
	 * equal or greater than the number of validation branches mentioned in the preferences. 
	 * @return true if the number of validation branches are >= validation branch value in the preference otherwise it returns false.
	 */
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
	
	/**
	 * This function divides the total number of leaves in the marker by the value given in the checksum preference. Code is valid if the modulo is 0.
	 * @return true if the number of leaves are divisible by the checksum value otherwise false.
	 */
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
