package uk.ac.horizon.tableware;

import java.util.List;

import uk.ac.horizon.tableware.TWSurfaceView.OnMarkerDetectedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class TablewareActivity extends Activity implements OnMarkerDetectedListener{
        
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    
    public static int viewMode  = VIEW_MODE_MARKER;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        initTWSurfaceViewListener();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemDetectMarker = menu.add(R.string.detect_marker);
        mItemDetectMarkerDebug = menu.add(R.string.detect_marker_debug);
        mItemPreference = menu.add(R.string.view_preferences);
        return true;
    }
    
    @Override
    public void onPause(){
    	stopMarkerDetectionProcess();
    	super.onPause();
    	
    }
    
    @Override
    public void onStop(){
    	stopMarkerDetectionProcess();
    	super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemDetectMarker)
            viewMode = VIEW_MODE_MARKER;
        else if (item == mItemDetectMarkerDebug)
        	viewMode = VIEW_MODE_MARKER_DEBUG;
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
    
    private void stopMarkerDetectionProcess(){
    	TWSurfaceView surfaceView = (TWSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.stopProcessing();
    }
   
    public void onMarkerDetected(List<DtouchMarker> markers){
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		startActivity(intent);
	}
      
    private void initTWSurfaceViewListener(){
    	TWSurfaceView surfaceView = (TWSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.setOnMarkerDetectedListener(this);
    }
    
    
    
}