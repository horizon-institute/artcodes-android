package uk.ac.horizon.tableware;

import org.opencv.core.Rect;

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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class TWCameraActivity extends FragmentActivity implements OnMarkerDetectedListener, OnMarkerPopupWindowListener{
    
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final String TAG = "Tableware::TablewareActivity";
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    private MenuItem	mItemMember;    //private List<DtouchMarker> mCurrentMarkers;
    private TWMarkerSurfaceView mMarkerSurfaceView;
        
    public static int viewMode  = VIEW_MODE_MARKER;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        displaySplashScreen();
        setContentView(R.layout.markercamera);
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
    	super.onResume();
    	Log.d(TAG, "On Resume");
    	startMarkerDetectionProcess();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(TAG, "On Resume");
    	stopMarkerDetectionProcess();
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
    	mMarkerSurfaceView.stopProcessing();
    }
    
    private void startMarkerDetectionProcess(){
    	mMarkerSurfaceView.startProcessing();
    }
    
    public void onMarkerDetected(final DtouchMarker marker){
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			showProgressControls();
    			new DtouchMarkerWebServicesTask().execute(marker.getCodeKey());
    		}
    	});
    }
    
    public void displayMarkerDetailFromWebService(DtouchMarker marker){
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		Bundle markerBundle = DtouchMarker.createMarkerBundleFromCode(marker);
		intent.putExtras(markerBundle);
		startActivity(intent);
	}
    
    private void initTWSurfaceViewListener(){
    	mMarkerSurfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	mMarkerSurfaceView.setOnMarkerDetectedListener(this);
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
    
    private void onMarkerDownloaded(DtouchMarker marker){
    	hideProgressControls();
    	displayMarkerPopupWindow(marker);
    }
    
    private void displayMarkerPopupWindow(DtouchMarker marker){
    	Rect rect = mMarkerSurfaceView.getMarkerPosition();
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	MarkerPopupWindow markerPopupWindow = new MarkerPopupWindow(frameLayout,marker);
    	markerPopupWindow.setOnMarkerPopupWindowListener(this);
    	Point location = new Point(rect.x,rect.y);
    	Point size = new Point(rect.width, rect.height);
    	markerPopupWindow.show(location, size);
    }
    
    
    //OnMarkerPopupWindowListener methods.
    public void onDismissedSelected(DtouchMarker marker){
    	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    }
    
	public void onBrowseMarkerSelected(DtouchMarker marker){
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
		stopMarkerDetectionProcess();
		displayMarkerDetailFromWebService(marker);
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