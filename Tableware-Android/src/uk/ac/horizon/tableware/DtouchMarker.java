package uk.ac.horizon.tableware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;

import android.os.Bundle;

public class DtouchMarker {
	private Mat mComponent;
	private int mIndex;
	private List<Integer> mCode;
	private String mURL;
	private String mDesc;

	
	//constructors
	DtouchMarker(){
		super();
	}
	
	DtouchMarker(List<Integer> code){
		mCode = new ArrayList<Integer>(code);
	}
	
	DtouchMarker(List<Integer> code, String url, String desc){
		mCode = new ArrayList<Integer>(code);
		mURL = url;
		mDesc = desc;
	}
	
	DtouchMarker(String code, String url, String desc){
		mCode = getCodeArrayFromString(code);
		mURL = url;
		mDesc = desc;
	}
	
	//set and get functions.
	public void setComponent(Mat component){
		if (mComponent != null)
			mComponent.release();
		mComponent = component.clone();
	}
	
	public Mat getComponent(){
		return mComponent;
	}
	
	public int getComponentIndex(){
		return mIndex;
	}
	
	public void setComponentIndex(int componentIndex){
		mIndex = componentIndex;
	}
		
	public List<Integer> getCode(){
		return mCode;
	}
	
	public void setCode(List<Integer> code){
		if (mCode != null){
			mCode.clear();
			mCode = null;
		}
		mCode = new ArrayList<Integer>(code);
	}
	
	public String getURL(){
		return mURL;
	}
	
	public void setURL(String url){
		mURL = url;
	}
	
	public String getDescription(){
		return mDesc;
	}
	
	public void setDescription(String desc){
		mDesc = desc;
	}
	
	public String getCodeKey(){
		String codeKey = null;
		if (mCode != null)
			codeKey = codeArrayToString(mCode);
		return codeKey;
	}
	
    private String codeArrayToString(List<Integer> codes){
    	StringBuffer code = new StringBuffer();
    	for(int i = 0; i < codes.size(); i++){
    		if (i > 0)
    			code.append(":");
    		code.append(codes.get(i));
    	}
    	return code.toString();
    }
    
    private static List<Integer> getCodeArrayFromString(String code){
    	String tmpCodes[] = code.split(":");
    	List<Integer> codes = new ArrayList<Integer>(); 
    	for (int i = 0; i < tmpCodes.length; i++){
    		codes.add(new Integer(tmpCodes[i]));
    	}
    	return codes;	
    }
    
    public boolean isCodeEqual(DtouchMarker marker){
    	String thisCode = this.getCodeKey();
    	String compareCode = marker.getCodeKey();
    	if (thisCode.compareTo(compareCode) == 0)
    		return true;
    	else
    		return false;
    }
    
	public static Bundle createMarkerBundleFromCode(DtouchMarker marker){
	   	Bundle markerBundle = new Bundle();
	   	markerBundle.putString("Code", marker.getCodeKey());
	   	return markerBundle;
	}
	
	public static DtouchMarker createMarkerFromBundle(Bundle markerBundle){
		String markerCode = markerBundle.getString("Code");
		return new DtouchMarker(getCodeArrayFromString(markerCode));
	}
}
