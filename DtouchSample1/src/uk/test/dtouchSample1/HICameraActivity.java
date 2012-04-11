package uk.test.dtouchSample1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class HICameraActivity extends Activity{
    
    static final int DIALOG_MARKER_DETECTION_ID = 0;
    private static final String TAG = "Tableware::TablewareActivity";
            
    private HIMarkerSurfaceView mMarkerSurfaceView;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.markercamera);
        mMarkerSurfaceView = (HIMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
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
    
    private void stopMarkerDetectionProcess(){
    	mMarkerSurfaceView.stopProcessing();
    }
    
    private void startMarkerDetectionProcess(){
    	mMarkerSurfaceView.startProcessing();
    }
    
}