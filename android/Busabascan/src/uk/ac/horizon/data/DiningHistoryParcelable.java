package uk.ac.horizon.data;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class DiningHistoryParcelable implements Parcelable {
	
	private List<TWDiningHistoryItem> mDiningHistory;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeTypedList(mDiningHistory);
	}
	
	public static final Parcelable.Creator<DiningHistoryParcelable> CREATOR =
			new Parcelable.Creator<DiningHistoryParcelable>() {
				public DiningHistoryParcelable createFromParcel (Parcel in){
					return new DiningHistoryParcelable(in);
				}
				
				public DiningHistoryParcelable[] newArray(int size){
					return new DiningHistoryParcelable[size];
				}
			};
	
	private DiningHistoryParcelable(Parcel in){
		in.readTypedList(mDiningHistory, TWDiningHistoryItem.CREATOR);
	}
	
	 

}
