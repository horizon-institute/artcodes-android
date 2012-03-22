package uk.ac.horizon.dtouch;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import android.os.Bundle;

public class DtouchMarker {
	private Mat mComponent;
	private int mIndex;
	private List<Integer> mCode;
	private String mURL1;
	private String mURL2;
	private String mURL3;
	private String mTitle;
	private String mType;
	private List<TWDiningHistoryItem> mDiningHistory;
	
	//constructors
	public DtouchMarker(){
		super();
	}
	
	public DtouchMarker(List<Integer> code){
		mCode = new ArrayList<Integer>(code);
	}
	
	public DtouchMarker(List<Integer> code, String url, String title){
		mCode = new ArrayList<Integer>(code);
		mURL1 = url;
		mTitle = title;
	}
	
	public DtouchMarker(String code, String url, String title){
		mCode = getCodeArrayFromString(code);
		mURL1 = url;
		mTitle = title;
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
	
	public void setCode(String code){
		this.setCode(getCodeArrayFromString(code));
	}
	
	public String getURL1(){
		return mURL1;
	}
	
	public void setURL1(String url){
		mURL1 = url;
	}
	
	public String getURL2(){
		return mURL2;
	}
	
	public void setURL2(String url){
		mURL2 = url;
	}
	
	public String getURL3(){
		return mURL3;
	}
	
	public void setURL3(String url){
		mURL3 = url;
	}
		
	public String getTitle(){
		return mTitle;
	}
	
	public void setTitle(String title){
		mTitle = title;
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
	
	public static Bundle createMarkerBundle(DtouchMarker marker){
		Bundle markerBundle = new Bundle();
		if ((marker.getCodeKey() != null) || (marker.getTitle() != null)){
			if (marker.getCode() != null)
				markerBundle.putString("Code", marker.getCodeKey());
			if (marker.getTitle() != null){
				markerBundle.putString("Title", marker.getTitle());
			}
			if (marker.getType() != null){
				markerBundle.putString("Type", marker.getType());
			}
			if (marker.getURL1() != null){
				markerBundle.putString("URL1", marker.getURL1());
			}
			if (marker.getURL2() != null){
				markerBundle.putString("URL2", marker.getURL2());
			}
			if (marker.getURL3() != null){
				markerBundle.putString("URL3", marker.getURL3());
			}
		}
		return markerBundle;
	}
	
	public static DtouchMarker createMarkerFromBundle(Bundle markerBundle){
		DtouchMarker marker = null;
		if (markerBundle.getString("Code") != null || markerBundle.getString("Title") != null){
			marker = new DtouchMarker();
			if (markerBundle.getString("Code") != null)
				marker.setCode(markerBundle.getString("Code"));
			if (markerBundle.getString("Title") != null)
				marker.setTitle(markerBundle.getString("Title"));
			if (markerBundle.getString("Type") != null)
				marker.setType(markerBundle.getString("Type"));
			if (markerBundle.getString("URL1") != null)
				marker.setURL1(markerBundle.getString("URL1"));
			if (markerBundle.getString("URL2") != null)
				marker.setURL2(markerBundle.getString("URL2"));
			if (markerBundle.getString("URL3") != null)
				marker.setURL3(markerBundle.getString("URL3"));
		}
		return marker;
	}
	
	public void setType(String type){
		mType = type;
	}
	
	public String getType(){
		return mType;
	}
	
	public void setDiningHistory(List<TWDiningHistoryItem> history){
		mDiningHistory = new ArrayList<TWDiningHistoryItem>(history);
	}
	
	public List<TWDiningHistoryItem> getDiningHistory(){
		return mDiningHistory;
	}
}
