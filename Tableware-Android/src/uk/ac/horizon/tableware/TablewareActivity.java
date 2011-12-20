package uk.ac.horizon.tableware;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class TablewareActivity extends Activity {
        
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_TILE  = 1;
    public static final int VIEW_MODE_EDGES = 2;
        
    private MenuItem	mItemDetectMarkers;
    private MenuItem	mItemTileImage;
    private MenuItem    mItemPreviewEdges;
    private MenuItem	mItemPreference;
    
    public static int viewMode  = VIEW_MODE_MARKER;
    private boolean mVisible;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        initScan();
    }
    
    private void initScan(){
    	final Button scanButton = (Button) findViewById(R.id.scanButton);
    	scanButton.setOnClickListener(new View.OnClickListener() {
    	    public void onClick(View v) {
    	    	if (mVisible)
    	    		hideProgressControls();
    	    	else
    	    		showProgressControls();
     	    }
    	});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemDetectMarkers = menu.add("Detect Markers");
        mItemTileImage = menu.add("Tile Image");
        mItemPreviewEdges = menu.add("Preview Edges");
        mItemPreference = menu.add("View Preferences");
        return true;
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemDetectMarkers)
            viewMode = VIEW_MODE_MARKER;
        else if (item == mItemTileImage)
        	viewMode = VIEW_MODE_TILE;
        else if (item == mItemPreviewEdges)
            viewMode = VIEW_MODE_EDGES;
        else if (item == mItemPreference)
        {
        	
        	displayPreferences();
        }
        return true;
    }
    
    private void displayPreferences(){
    	Intent intent = new Intent(this, TWPreferenceActivity.class);
		startActivity(intent);
    }
    
    private void showProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout1);
    	LayoutInflater inflater = getLayoutInflater();
    	inflater.inflate(R.layout.scanprogress, frameLayout);
    	mVisible = true;
   }
    
    private void hideProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout1);
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.scanProgressBar);
    	frameLayout.removeView(progressBar);
    	progressBar = null;
    	mVisible = false;
    }
    
}