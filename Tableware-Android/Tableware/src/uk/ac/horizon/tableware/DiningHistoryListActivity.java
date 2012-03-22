package uk.ac.horizon.tableware;

import java.util.List;

import uk.ac.horizon.dtouch.TWDiningHistoryItem;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DiningHistoryListActivity extends ListActivity {
    private List<TWDiningHistoryItem> mDiningHistory;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDiningHistoryList();
        setListAdapter(new TWDiningHistoryListAdapter(this, mDiningHistory));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> listView, View listItem, int position,long id) {
				Toast.makeText(getApplicationContext(), ((TextView) listItem).getText(), Toast.LENGTH_SHORT).show();
			}
        });
    }
	
	private void initDiningHistoryList(){
		Intent intent = getIntent();
		mDiningHistory = intent.getParcelableArrayListExtra(getString(R.string.dining_history));
	}
}
