package uk.ac.horizon.dtouchMobile;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the d-touch code and the index of the root component.
 * @author pszsa1
 *
 */
public class DtouchMarker {
	private int mIndex;
	private List<Integer> mCode;
	
	//constructors
	public DtouchMarker(){
		super();
	}
	
	public DtouchMarker(List<Integer> code){
		mCode = new ArrayList<Integer>(code);
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
    
}