package uk.ac.horizon.busabascan;

import java.util.List;

import org.opencv.core.Rect;

import uk.ac.horizon.busabascan.MarkerPopupWindow.OnMarkerPopupWindowListener;
import uk.ac.horizon.busabascan.TWMarkerSurfaceView.OnMarkerDetectedListener;
import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.data.HIPreferenceTableware;
import uk.ac.horizon.dtouchMobile.DtouchMarker;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class TWCameraMainActivity extends Activity implements OnMarkerDetectedListener, OnMarkerPopupWindowListener{
    
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
	public final static String RESTAURANT = "uk.ac.horizon.busabascan.RESTAURANT";
	public final static String DISH = "uk.ac.horizon.busabascan.DISH";

    private static final String MARKER_TYPE_FOOD = "food";
    private static final String MARKER_TYPE_OFFER = "offer";
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final String TAG = "Tableware::TablewareActivity";
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    private TWMarkerSurfaceView mMarkerSurfaceView;
    
    //The mappings between codes and 
    //private static final DtouchMarker BIRD_ST_MARKER = new DtouchMarker("1:1:2:2:6");
    //private static final DtouchMarker PANDAN_CHICK_MARKER = new DtouchMarker("1:1:1:3:6");
    //private static final DtouchMarker CHAR_DUCK_MARKER = new DtouchMarker("1:1:3:3:4");
    private static final DtouchMarker OLD_ST_MARKER = new DtouchMarker("1:1:2:3:5");
    private static final DtouchMarker PANDAN_CHICK_MARKER = new DtouchMarker("1:1:1:1:2");
    private static final DtouchMarker CHAR_DUCK_MARKER = new DtouchMarker("1:1:2:4:4");
    private static final DtouchMarker PLACEMAT1_MARKER = new DtouchMarker("1:1:1:4:5");
    private static final DtouchMarker PLACEMAT2_MARKER = new DtouchMarker("1:1:3:3:4");
        
    public static int viewMode  = VIEW_MODE_MARKER;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //displaySplashScreen();
        setContentView(R.layout.markercamera);
        initTWSurfaceView();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemDetectMarker = menu.add(R.string.detect_marker);
        mItemDetectMarkerDebug = menu.add(R.string.detect_marker_debug);
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
        return true;
    }
    
	public void onMemberBtnClick(View sender){
		Intent intent = new Intent(this, TWMembershipActivity.class);
		startActivity(intent);
	}
    
    private void displayPreferences(){
    	Intent intent = new Intent(this, TWPreferenceActivity.class);
		startActivity(intent);
    }
    
    private void stopMarkerDetectionProcess(){
    	mMarkerSurfaceView.stopProcessing();
    }
    
    private void startMarkerDetectionProcess(){
    	mMarkerSurfaceView.startProcessing();
    }
    
    public void onMarkerDetected(final List<DtouchMarker> markers){
    	
    	DtouchMarker marker = markers.get(0);
    	DtouchMarker marker2 = null;
    	if (markers.size() > 1) {marker2 = markers.get(1);}
    	
    	if (marker.isCodeEqual(OLD_ST_MARKER))
    	{
    		//We're outside Bird Street.
    		displayOutsideRestaurant("Bird St");
    	}
    	else if (marker.isCodeEqual(PANDAN_CHICK_MARKER) && marker2 == null)
    	{
    		//We're looking at Pandan Chicken.
    		displayDish("Pandan chicken");
    	}
    	else if (marker.isCodeEqual(CHAR_DUCK_MARKER) && marker2 == null)
    	{
    		//We're looking at Char Grilled Duck.
    		displayDish("Char-grilled duck");
    	}
    	else if (markers.contains(PANDAN_CHICK_MARKER) && markers.contains(CHAR_DUCK_MARKER))
    	{
    		displayMenu();
    	}
    	else if (marker.isCodeEqual(PLACEMAT1_MARKER))
    	{
    		//We're looking at Char Grilled Duck.
    		displayPlacemat(1);
    	}
    	else if (marker.isCodeEqual(PLACEMAT2_MARKER))
    	{
    		//We're looking at Char Grilled Duck.
    		displayPlacemat(2);
    	}
    	else
    	{
        	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    	}
    	//RNM Don't need calls to the backend for now 
    	/*
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			//showProgressControls();
    			getMarker(marker.getCodeKey());
    			//new DtouchMarkerWebServicesTask().execute(marker.getCodeKey());
    		}
    	});
    	*/
    }
    
    private void displayPlacemat(int placemat) {
		// Start the new activity
		Intent intent = new Intent(this, TWSeatedActivity.class);
    	startActivity(intent);		
	}

	//Start the menu activity
    private void displayMenu() {
		// Start the new activity
		Intent intent = new Intent(this, TWMenuActivity.class);
		startActivity(intent);
	}

	void getMarker(String code){
    	DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener(){
    		@Override
			public void onMarkerDownloadError() {
				markerDownloadWithError();
				
			}

			@Override
			public void onMarkerDownloaded(DataMarker marker) {
				markerDownloaded(marker);
			}
    	});
    	dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, null);
    }
    
    public void displayMarkerDetail(DataMarker marker){
    	if (marker.getType().compareToIgnoreCase(MARKER_TYPE_FOOD) == 0){
    		//displayDish(marker);
    	}else if (marker.getType().compareToIgnoreCase(MARKER_TYPE_OFFER) == 0){
    		displayOffer(marker);
    	}
    }
    
    public void displayOutsideRestaurant (String location){
    	Intent intent = new Intent(this, TWOutsideActivity.class);
    	intent.putExtra(RESTAURANT, location);
    	startActivity(intent);
    }
    
    // When button is pushed
    public void displayOutside (View view){
    	Intent intent = new Intent(this, TWOutsideActivity.class);
    	startActivity(intent);
    }
    
    private void displayDish(String dish){
		Intent intent = new Intent(this, TWDishActivity.class);
		intent.putExtra(DISH, dish);
		startActivity(intent);
	}
    
    private void displayOffer(DataMarker marker){
    	Intent intent = new Intent(this, TWOfferActivity.class);
		Bundle markerBundle = DataMarker.createMarkerBundle(marker);
		intent.putExtras(markerBundle);
		startActivity(intent);
    }
        
    private void initTWSurfaceView(){
    	mMarkerSurfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	mMarkerSurfaceView.setOnMarkerDetectedListener(this);
    	mMarkerSurfaceView.setPreference(new HIPreferenceTableware(this));
    }

/*    
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
*/    
    private void markerDownloaded(DataMarker marker){
//    	hideProgressControls();
    	if (marker != null)
    		displayMarkerPopupWindow(marker);
    	else
    		mMarkerSurfaceView.stopDisplayingDetectedMarker();
    }
    
    private void markerDownloadWithError(){
 //   	hideProgressControls();
    	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    	MessageDialog.showMessage(R.string.marker_download_error, this);
    	
    }
        
    private void displayMarkerPopupWindow(DataMarker marker){
  /*  	Rect rect = mMarkerSurfaceView.getMarkerPosition();
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	MarkerPopupWindow markerPopupWindow = new MarkerPopupWindow(frameLayout,marker);
    	markerPopupWindow.setOnMarkerPopupWindowListener(this);
    	Point location = new Point(rect.x,rect.y);
    	Point size = new Point(rect.width, rect.height);
    	markerPopupWindow.show(location, size);*/
    }
    
    
    //OnMarkerPopupWindowListener methods.
    public void onDismissedSelected(DataMarker marker){
    	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    }
    
	public void onBrowseMarkerSelected(DataMarker marker){
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
		stopMarkerDetectionProcess();
		displayMarkerDetail(marker);
	}
    
}
