package uk.ac.horizon.busabascan;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class TWMenuActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twmenu);
		new Handler();
		ExpandableListAdapter mAdapter;
	    ExpandableListView elView = (ExpandableListView) findViewById(R.id.expandableListView2);
	    mAdapter = new MenuListAdapter(this);
	    elView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twmenu, menu);
        return true;
    }
}
