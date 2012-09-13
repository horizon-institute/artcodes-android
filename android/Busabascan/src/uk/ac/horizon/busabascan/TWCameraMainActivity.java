package uk.ac.horizon.busabascan;

import java.util.List;

import com.facebook.android.R;

import uk.ac.horizon.busabascan.MarkerPopupWindow.OnMarkerPopupWindowListener;
import uk.ac.horizon.busabascan.TWMarkerSurfaceView.OnMarkerDetectedListener;
import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.data.HIPreferenceTableware;
import uk.ac.horizon.dtouchMobile.DtouchMarker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private Handler progressBarHandler = new Handler();
    
    //The mappings between codes and 
    private static final DtouchMarker OLD_ST_MARKER = new DtouchMarker("1:1:2:3:5");
    private static final DtouchMarker PANDAN_CHICK_MARKER = new DtouchMarker("1:1:1:1:2");
    private static final DtouchMarker CHAR_DUCK_MARKER = new DtouchMarker("1:1:2:4:4");
    private static final DtouchMarker PLACEMAT1_MARKER = new DtouchMarker("1:1:1:4:5");
    private static final DtouchMarker PLACEMAT2_MARKER = new DtouchMarker("1:1:3:3:4");
    
    enum CodeType {NONE, RESTAURANT, DISH1, DISH2, PLACEMAT1, PLACEMAT2, MENU, UNKNOWN};
        
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
    
    private CodeType interpretMarkers(final List<DtouchMarker> markers){
    	
    	CodeType ret;
    	if (markers == null || markers.size() == 0)
    	{
    		ret = CodeType.NONE;    		
    	}
    	else if (markers.contains(OLD_ST_MARKER))
    	{
    		ret = CodeType.RESTAURANT;
    	}
    	else if (markers.contains(PLACEMAT1_MARKER))
    	{
    		ret = CodeType.PLACEMAT1;
    	}
    	else if (markers.contains(PLACEMAT2_MARKER))
    	{
    		ret = CodeType.PLACEMAT2;
    	}
    	else if (markers.contains(PANDAN_CHICK_MARKER) && markers.contains(CHAR_DUCK_MARKER))
    	{
    		ret = CodeType.MENU;
    	}
    	else if (markers.contains(PANDAN_CHICK_MARKER))
    	{
    		ret = CodeType.DISH1;
    	}
    	else if (markers.contains(CHAR_DUCK_MARKER))
    	{
    		ret = CodeType.DISH2;
      	}
    	else
    	{
    		ret = CodeType.UNKNOWN;
     	}
		return ret;
    }

    
    public void onMarkerDetected(final List<DtouchMarker> markers){
    	
    	CodeType code = interpretMarkers(markers);
    	
    	switch (code){
    	case RESTAURANT:
    		//We're outside Old Street.
    		displayOutsideRestaurant("Old St");
    		break;
    	case PLACEMAT1:
    		displayPlacemat(1);
    		break;
    	case PLACEMAT2:
    		displayPlacemat(2);
    		break;
    	case MENU:
    		displayMenu();
    		break;
    	case DISH1:
    		//We're looking at Pandan Chicken.
    		displayDish("Pandan chicken");
    		break;
    	case DISH2:
    		//We're looking at Char Grilled Duck.
    		displayDish("Char-grilled duck");
    		break;
    	default:
        	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    	}
     }
    
    private String getDetectionGuess()
    {
    	String ret;
    	
   	    List<DtouchMarker> markers = mMarkerSurfaceView.guessAtMarkers();
		CodeType code = interpretMarkers(markers);
    	
    	switch (code){
    	case RESTAURANT:
    		ret = "Old Street";
    		break;
    	case PLACEMAT1:
    		ret = "Placemat";
    		break;
    	case PLACEMAT2:
    		ret = "Placemat";
    		break;
    	case MENU:
    		ret = "Menu";
    		break;
    	case DISH1:
    		ret = "Pandan chicken";
    		break;
    	case DISH2:
    		ret = "Char-grilled duck";
    		break;
    	case UNKNOWN:
    		ret = "Beats me!";
    		break;
    	default:
        	ret = "";
    	}
    	return ret;
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
    	
    	
    	final ProgressBar pb = (ProgressBar) this.findViewById(R.id.integrationBar);
    	final TextView guess = (TextView) this.findViewById(R.id.textViewGuess);
    	
    	new Thread(new Runnable() {
			public void run() {
				while(true){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
									    
				   	progressBarHandler.post(new Runnable() {

						public void run() {    	
							Integer pendpercent = mMarkerSurfaceView.getPendingPercent();

						    if (pendpercent > 0)
					    	{
						       if (pb.getVisibility() != View.VISIBLE)
						       {
						            pb.setVisibility(View.VISIBLE);
						       }
					    	   pb.setProgress(pendpercent);
					    	}
					    	else
					    	{
					    		pb.setVisibility(View.GONE);
					    	}
						    
						    guess.setText(getDetectionGuess());
						}
			    	});				
				}				
			} 
    	}).start();
    	
 
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
