package uk.ac.horizon.data;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

public class DataMarker {
	private String mCode;
	private String mURL1;
	private String mURL2;
	private String mURL3;
	private String mTitle;
	private String mType;
	private List<TWDiningHistoryItem> mDiningHistory;
	
	//constructors
	public DataMarker(){
		super();
	}
	
	public DataMarker(String code){
		mCode = code;
	}
	
	public DataMarker(String code, String url, String title){
		mCode = code;
		mURL1 = url;
		mTitle = title;
	}
	
	public String getCode(){
		return mCode;
	}
	
	public void setCode(String code){
		mCode = code;
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
	
	public static Bundle createMarkerBundleFromCode(DataMarker marker){
	   	Bundle markerBundle = new Bundle();
	   	markerBundle.putString("Code", marker.getCode());
	   	return markerBundle;
	}
	
	public static Bundle createMarkerBundle(DataMarker marker){
		Bundle markerBundle = new Bundle();
		if ((marker.getCode() != null) || (marker.getTitle() != null)){
			if (marker.getCode() != null)
				markerBundle.putString("Code", marker.getCode());
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
	
	public static DataMarker createMarkerFromBundle(Bundle markerBundle){
		DataMarker marker = null;
		if (markerBundle.getString("Code") != null || markerBundle.getString("Title") != null){
			marker = new DataMarker();
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