package uk.ac.horizon.tableware;

import java.util.List;

import com.facebook.android.R;

import uk.ac.horizon.dtouch.TWDiningHistoryItem;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

public class TWDiningHistoryListActivity extends ListActivity {
	
	private List<TWDiningHistoryItem> mDiningHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dining_history_list);
		initDiningHistory();
		setListAdapter(new TWDiningHistoryListAdapter(this, mDiningHistory));
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		
	}
	
	private void initDiningHistory(){
		Intent bundle = this.getIntent();
		mDiningHistory = bundle.getParcelableArrayListExtra(getString(R.string.dining_history));
	}
}
