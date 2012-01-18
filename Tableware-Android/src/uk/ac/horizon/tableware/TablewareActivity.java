package uk.ac.horizon.tableware;

import java.util.List;

import uk.ac.horizon.tableware.TWMarkerSurfaceView.OnMarkerDetectedListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class TablewareActivity extends FragmentActivity implements OnMarkerDetectedListener{
        
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final int FIRST_MARKER_INDEX = 0;
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    private MenuItem	mItemMember;
    
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
        mItemMember = menu.add(R.string.menu_member_info);
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
           	displayPreferences();
        else if (item == mItemMember)
        	displayMemberInfo();
        return true;
    }
    
    private void displayPreferences(){
    	Intent intent = new Intent(this, TWPreferenceActivity.class);
		startActivity(intent);
    }
    
    private void displayMemberInfo(){
    	Intent intent = new Intent(this, TWMemberActivity.class);
    	startActivity(intent);
    }
    
    private void stopMarkerDetectionProcess(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.stopProcessing();
    }
    
    public void onMarkerDetected(List<DtouchMarker> markers){
    	stopMarkerDetectionProcess();
    	displayMarkerResult(markers);
	}
    
    private void displayMarkerResult(List<DtouchMarker> markers){
     	Intent intent = new Intent(this,TWMarkerResultActivity.class);
    	DtouchMarker marker = markers.get(FIRST_MARKER_INDEX);
    	Bundle markerBundle = DtouchMarker.createMarkerBundleFromCode(marker);
       	intent.putExtras(markerBundle);
    	startActivity(intent);
    }
    
    private void initTWSurfaceViewListener(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.setOnMarkerDetectedListener(this);
    }
    
}