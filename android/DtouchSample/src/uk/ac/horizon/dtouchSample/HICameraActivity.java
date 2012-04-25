package uk.ac.horizon.dtouchSample;

import org.opencv.core.Rect;

import uk.ac.horizon.dtouchMobile.DtouchMarker;
import uk.ac.horizon.dtouchSample.DataMarkerWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.dtouchSample.HIMarkerSurfaceView.OnMarkerDetectedListener;
import uk.ac.horizon.dtouchSample.MarkerPopupWindow.OnMarkerPopupWindowListener;
import uk.ac.horizon.dtouchSample.MarkerPopupWindowWithImage.OnMarkerPopupWindowWithImageListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class HICameraActivity extends Activity implements OnMarkerDetectedListener, OnMarkerPopupWindowListener, OnMarkerPopupWindowWithImageListener{
    
    public static final int	VIEW_MODE_MARKER  = 0;
    public static final int	VIEW_MODE_MARKER_DEBUG  = 1;
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final String TAG = "Tableware::TablewareActivity";
            
    private MenuItem	mItemDetectMarker;
    private MenuItem	mItemDetectMarkerDebug;
    private MenuItem	mItemPreference;
    private HIMarkerSurfaceView mMarkerSurfaceView;
        
    public static int viewMode  = VIEW_MODE_MARKER;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //displaySplashScreen();
        setContentView(R.layout.markercamera);
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
    
    private void displayPreferences(){
    	Intent intent = new Intent(this, HIPreferenceActivity.class);
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
    			getMarker(marker.getCodeKey());
    			//new DtouchMarkerWebServicesTask().execute(marker.getCodeKey());
    		}
    	});
    }
    
    void getMarker(String code){
    	DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener(){
    		public void onMarkerDownloaded(DataMarker marker){
    			markerDownloaded(marker);
    		}

			@Override
			public void onMarkerDownloadError() {
				markerDownloadWithError();
			}
    	});
    	dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, null);
    }
    
    public void displayMarkerDetail(DataMarker marker){
    	displayMarker(marker);
    }
    
    private void initTWSurfaceViewListener(){
    	mMarkerSurfaceView = (HIMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
    	mMarkerSurfaceView.setOnMarkerDetectedListener(this);
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
    
    private void markerDownloaded(DataMarker marker){
    	hideProgressControls();
    	if (marker != null && marker.getServiceId() > 0)
    		displayMarkerPopupWindow(marker);
    	else if (marker != null && marker.getUri() != null)
    		displayMarkerPopupWindowWithImage(marker);
    	else if (marker != null && marker.getTitle() != null)
    		displayMarkerPopupWindow(marker); 
    	else
    		mMarkerSurfaceView.stopDisplayingDetectedMarker();
    }
    
    private void markerDownloadWithError(){
    	hideProgressControls();
    	MessageDialog.showMessage(R.string.downloadErrorMsg, this);
    }
    
    private void displayMarkerPopupWindow(DataMarker marker){
    	Rect rect = mMarkerSurfaceView.getMarkerPosition();
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	MarkerPopupWindow markerPopupWindow = new MarkerPopupWindow(frameLayout,marker);
    	markerPopupWindow.setOnMarkerPopupWindowListener(this);
    	Point location = new Point(rect.x,rect.y);
    	Point size = new Point(rect.width, rect.height);
    	markerPopupWindow.show(location, size);
    }
    
    private void displayMarkerPopupWindowWithImage(DataMarker marker){
    	Rect rect = mMarkerSurfaceView.getMarkerPosition();
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    	MarkerPopupWindowWithImage markerPopupWindow = new MarkerPopupWindowWithImage(frameLayout,marker, this);
    	markerPopupWindow.setOnMarkerPopupWindowListener(this);
    	Point location = new Point(rect.x,rect.y);  
    	Point size = new Point(rect.width, rect.height);
    	markerPopupWindow.show(location, size);
    }
    
    //OnMarkerPopupWindowWithImageListener methods.
    public void onImageDismissedSelected(DataMarker marker){
    	mMarkerSurfaceView.stopDisplayingDetectedMarker();
    }
    
	public void onImageBrowseMarkerSelected(DataMarker marker){
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
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
	
	//services function.
	private void displayMarker(DataMarker marker){
		switch (marker.getServiceId()){
		case DataMarker.YOU_TUBE:
			displayYouTube();
			break;
		case DataMarker.MUSIC:
			displayMusic();
			break;
		case DataMarker.CONTACTS:
			displayContacts();
			break;
		case DataMarker.MAIL:
			displayMail();
			break;
		default:
			break;
		}
	}

	private void displayYouTube(){
		String VIEDO_ID = "cKd8NXWwvKI";
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("vnd.youtube:"+ VIEDO_ID));
		startActivity(intent);
	}

	private void displayMusic(){
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}

	private void displayContacts(){
		Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
		startActivity(intent);
	}

	private void displayMail(){
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		/* Fill it with Data */
		intent.setType("plain/text");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"abc@email.com"});
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
		/* Send it off to the Activity-Chooser */
		this.startActivity(Intent.createChooser(intent, "Send mail..."));
	}
    
}