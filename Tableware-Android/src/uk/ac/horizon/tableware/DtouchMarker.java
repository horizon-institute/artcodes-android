package uk.ac.horizon.tableware;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class DtouchMarker {
	private Mat mComponent;
	private int mIndex;
	private List<Integer> mCode;


	DtouchMarker(Mat component, int componentIndex, List<Integer> code){
		mComponent = component.clone();
		mIndex = componentIndex;
		mCode = new ArrayList<Integer>(code);
	}
	
	public Mat getComponent(){
		return mComponent;
	}
	
	public List<Integer> getCode(){
		return mCode;
	}
	
	public int getComponentIndex(){
		return mIndex;
	}
	
}
