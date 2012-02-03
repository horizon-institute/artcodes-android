package uk.ac.horizon.tableware;

import uk.ac.horizon.tableware.MarkerPopupWindow.OnMarkerPopupWindowListener;
import uk.ac.horizon.tableware.TWMarkerSurfaceView.OnMarkerDetectedListener;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TablewareActivity extends FragmentActivity implements OnMarkerDetectedListener, OnMarkerPopupWindowListener{
    
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final String TAG = "Tableware::TablewareActivity";
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    private MenuItem	mItemMember;    //private List<DtouchMarker> mCurrentMarkers;
        
    public static int viewMode  = VIEW_MODE_MARKER;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        displaySplashScreen();
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
    public void onResume(){
    	Log.d(TAG, "On Resume");
    	startMarkerDetectionProcess();
    	super.onResume();
    }
    
    @Override
    public void onPause(){
    	Log.d(TAG, "On Resume");
    	stopMarkerDetectionProcess();
    	super.onPause();
    }
    
    public void onScanMarkerBtnClick(View view){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	//surfaceView.startScan();
    }
    
    /*
    @Override
    public void onStop(){
    	stopMarkerDetectionProcess();
    	super.onStop();
    }*/

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
    
    private void startMarkerDetectionProcess(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.startProcessing();
    }
    
    public void onMarkerDetected(final DtouchMarker marker){
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			showProgressControls();
    			new DtouchMarkerWebServicesTask().execute(marker.getCodeKey());
    			//stopMarkerDetectionProcess();
    			//displayMarkerResult(markers);
    			//mCurrentMarkers = markers;
    		}
    	});
    }
    
    public void onMarkerScanned(final DtouchMarker marker){
    	this.runOnUiThread(new Runnable(){
    	public void run(){
    		//stopMarkerDetectionProcess();
    		//displayMarkerResult(marker);
    		//mCurrentMarkers = markers;
    	}
    	});
    }
    /*
    public void onMarkerDetailBtnClick(View view){
    	if (mCurrentMarkers != null && mCurrentMarkers.size() > 0){
    		stopMarkerDetectionProcess();
    		displayMarkerResult(mCurrentMarkers);
    	}
    }*/
    
    private void displayMarkerResult(DtouchMarker marker){
     	Intent intent = new Intent(this,TWMarkerResultActivity.class);
    	Bundle markerBundle = DtouchMarker.createMarkerBundleFromCode(marker);
       	intent.putExtras(markerBundle);
    	startActivity(intent);
    }
    
    private void initTWSurfaceViewListener(){
    	TWMarkerSurfaceView surfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	surfaceView.setOnMarkerDetectedListener(this);
    }
    
    private void displaySplashScreen(){
    	final TWSplashScreenFragment frag = TWSplashScreenFragment.newInstance();
    	frag.show(this.getSupportFragmentManager(), "SPLASH_SCREEN");
    	final Handler handler = new Handler();
    	handler.postDelayed(new Runnable(){
    		public void run(){
    			frag.dismiss();
    		}
    	}, 2000);
    }
    
    private void showProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	LayoutInflater inflater = getLayoutInflater();
    	inflater.inflate(R.layout.scanprogress, frameLayout);
    }

    private void hideProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.scanProgressBar);
    	frameLayout.removeView(progressBar);
    	progressBar = null;
    }
    
    private void showMarkerLabel(String markerDesc){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	LayoutInflater inflater = getLayoutInflater();
    	inflater.inflate(R.layout.markerlabel, frameLayout);
    	TextView markerLabel = (TextView) findViewById(R.id.markerLabel);
    	markerLabel.setText(markerDesc);
    }
    
    private void onMarkerDownloaded(DtouchMarker marker){
    	showMarkerLabel(marker.getDescription());
    	hideProgressControls();
    	displayMarkerPopupWindow(marker);
    }
    
    private void displayMarkerPopupWindow(DtouchMarker marker){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	MarkerPopupWindow markerPopupWindow = new MarkerPopupWindow(frameLayout,marker);
    	markerPopupWindow.setOnMarkerPopupWindowListener(this);
    	markerPopupWindow.show(new Point(0,0));
    }
    
    //OnMarkerPopupWindowListener methods.
    public void onDismissedSelected(DtouchMarker marker){
    	
    }
    
	public void onBrowseMarkerSelected(DtouchMarker marker){
		
	}
    
    class DtouchMarkerWebServicesTask extends AsyncTask<String, Void, DtouchMarker> {
	   
	   protected DtouchMarker doInBackground(String... codes){
		   return DtouchMarkersDataSource.getDtouchMarkerUsingKey(codes[0]);
	   }

	   protected void onPostExecute(DtouchMarker marker){
		   onMarkerDownloaded(marker);
	   }
   }
    
}