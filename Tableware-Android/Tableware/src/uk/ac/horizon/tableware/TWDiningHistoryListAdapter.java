package uk.ac.horizon.tableware;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.horizon.dtouch.TWDiningHistoryItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class TWDiningHistoryListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<TWDiningHistoryItem> mDiningHistory;
		 
	public TWDiningHistoryListAdapter(Context context, List<TWDiningHistoryItem> diningHistory){
		mInflater = LayoutInflater.from(context);
		mDiningHistory = new ArrayList<TWDiningHistoryItem>(diningHistory);
	}
	
	@Override
	public int getCount() {
		return mDiningHistory.size();
	}

	@Override
	public Object getItem(int position) {
		return mDiningHistory.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.dish_history_list_item, null);
			holder = new ViewHolder();
			holder.dateDinedTextView = (TextView) convertView.findViewById(R.id.dateDined);
			holder.dishRatingBar = (RatingBar) convertView.findViewById(R.id.dishRating);
			holder.dishCommentsTextView = (TextView) convertView.findViewById(R.id.dishComments);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		TWDiningHistoryItem TWDiningHistoryItem = (TWDiningHistoryItem) getItem(position);
		
		String dateInLocalFormat = getDateInLocalFormat(TWDiningHistoryItem.mDate);
		if (dateInLocalFormat != null)
			holder.dateDinedTextView.setText(dateInLocalFormat);
		holder.dishRatingBar.setRating((float)TWDiningHistoryItem.mRating);
		holder.dishCommentsTextView.setText(TWDiningHistoryItem.mComments);
		return convertView;
	}
	
	private String getDateInLocalFormat(String dateTime){
		String dateInLocalFormat = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			Date date = sdf.parse(dateTime);
			dateInLocalFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(date);
			dateInLocalFormat = dateInLocalFormat.concat(" " + SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(date));
		} catch (java.text.ParseException e1) {
			e1.printStackTrace();
		}
		
		return dateInLocalFormat;
	}
	
	static class ViewHolder{
		TextView dateDinedTextView;
		RatingBar dishRatingBar;
		TextView dishCommentsTextView;
	}
}
