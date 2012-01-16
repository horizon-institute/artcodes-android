package uk.ac.horizon.tableware;

import java.util.List;

import uk.ac.horizon.tableware.TWMarkerSurfaceView.OnMarkerDetectedListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class TablewareActivity extends FragmentActivity implements OnMarkerDetectedListener{
        
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
    static final int DIALOG_MARKER_DETECTION_ID = 0;
            
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
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.stopProcessing();
    }
    
    private void resumeMarkerDetectionProcess(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.startProcessing();
    }
   
    public void onMarkerDetected(List<DtouchMarker> markers){
    	stopMarkerDetectionProcess();
    	
    	/*TWMarkerAlertDialogFragment frag = TWMarkerAlertDialogFragment.newInstance(R.string.app_name);
    	frag.show(this.getSupportFragmentManager(), "DIALOG");*/
    	Intent intent = new Intent(this,TWMarkerResultActivity.class);
    	DtouchMarker marker = markers.get(0);
    	Bundle markerBundle = marker.createMarkerBundleFromCode(marker);
       	intent.putExtras(markerBundle);
    	startActivity(intent);
	}
    
 
    
    public void displayMarkerDetail(TWMarkerAlertDialogFragment frag){
     	frag.dismiss();
    	Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
    	startActivity(intent);
    }
    
    public void cancelAlertDialogFragment(TWMarkerAlertDialogFragment frag){
    	frag.dismiss();
    	resumeMarkerDetectionProcess();
    }
    
    private void initTWSurfaceViewListener(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.setOnMarkerDetectedListener(this);
    }
    
   
}