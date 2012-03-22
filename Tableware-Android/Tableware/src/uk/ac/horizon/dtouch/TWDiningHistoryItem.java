package uk.ac.horizon.dtouch;

import android.os.Parcel;
import android.os.Parcelable;

public class TWDiningHistoryItem implements Parcelable{
	public String mDate;
	public double mRating;
	public String mComments;
	
	public static final Parcelable.Creator<TWDiningHistoryItem> CREATOR = new Parcelable.Creator<TWDiningHistoryItem>(){
		
		public TWDiningHistoryItem createFromParcel(Parcel in){
			return new TWDiningHistoryItem(in);
		}
		
		public TWDiningHistoryItem[] newArray(int size){
			return new TWDiningHistoryItem[size];
		}
	};
	
	public TWDiningHistoryItem(String date, double rating, String comments){
		this.mDate = date;
		this.mRating = rating;
		this.mComments = comments;
	}
	
	private TWDiningHistoryItem(Parcel in){
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mDate);
		out.writeDouble(mRating);
		out.writeString(mComments);
	}
	
	public void readFromParcel(Parcel in){
		mDate = in.readString();
		mRating = in.readDouble();
		mComments = in.readString();
	}
}
