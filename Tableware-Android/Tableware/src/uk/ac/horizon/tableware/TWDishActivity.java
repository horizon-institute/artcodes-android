package uk.ac.horizon.tableware;

import java.util.ArrayList;
import com.facebook.android.FacebookPostDialogListener;
import com.facebook.android.R;
import com.facebook.android.Utility;

import uk.ac.horizon.dtouch.DtouchMarker;
import uk.ac.horizon.dtouch.DtouchMarkerDataWebServices;
import uk.ac.horizon.dtouch.DtouchMarkerImageWebServices;
import uk.ac.horizon.dtouch.DtouchMarkerDataWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.dtouch.DtouchMarkerImageWebServices.MarkerImageDownloadRequestListener;
import uk.ac.horizon.dtouch.DtouchMarkerWebServicesURL;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TWDishActivity extends Activity {
	private DtouchMarker dtouchMarker;
	ProgressDialog mSpinner;
			
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dish);
		setupUserControls();
		setupSpinner();
		initMarker();
		setActivityCaption();
		if (Utility.userUID != null)
			getPersonMarkerDetail(dtouchMarker.getCodeKey(), dtouchMarker.getTitle(), Utility.userUID);
		else
			getPersonMarkerDetail(dtouchMarker.getCodeKey(), dtouchMarker.getTitle(), null);
	}
	
	private void initMarker(){
		Intent intent = getIntent();
		Bundle markerBundle = intent.getExtras();
		dtouchMarker = DtouchMarker.createMarkerFromBundle(markerBundle);
	}
	
    void showInLineProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.dishImageframeLayout);
    	LayoutInflater inflater = getLayoutInflater();
    	inflater.inflate(R.layout.scanprogress, frameLayout);
    }
    
    void hideInLineProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.dishImageframeLayout);
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.scanProgressBar);
    	frameLayout.removeView(progressBar);
    	progressBar = null;
    }

	
	@Override 
	public void onResume(){
		super.onResume();
	}
	
	private void setActivityCaption(){
		TextView activityCaptionTextView = (TextView) findViewById(R.id.activityCaptionTextView);
		if (dtouchMarker != null){
			activityCaptionTextView.setText(dtouchMarker.getTitle());
		}
	}
	
	private void getMarkerImage(String markerCode){
		DtouchMarkerImageWebServices dtouchMarkerImageWebServices = new DtouchMarkerImageWebServices(new MarkerImageDownloadRequestListener(){
			public void onMarkerImageDownloaded(Bitmap bmp){
				hideInLineProgressControls();
				setDishImageView(bmp);
			}

			@Override
			public void onMarkerImageDownloadError() {
				hideInLineProgressControls();
				MessageDialog.showMessage(R.string.dish_image_download_error,TWDishActivity.this);
			}
		});
		showInLineProgressControls();
		dtouchMarkerImageWebServices.executeMarkerImageRequest(markerCode);
	}
	
	private void getDishImage(String dishTitle){
		DtouchMarkerImageWebServices dtouchMarkerImageWebServices = new DtouchMarkerImageWebServices(new MarkerImageDownloadRequestListener(){
			public void onMarkerImageDownloaded(Bitmap bmp){
				hideInLineProgressControls();
				setDishImageView(bmp);
			}

			@Override
			public void onMarkerImageDownloadError() {
				hideInLineProgressControls();
				MessageDialog.showMessage(R.string.dish_image_download_error,TWDishActivity.this);
			}
		});
		showInLineProgressControls();
		dtouchMarkerImageWebServices.executeDishImageRequest(dishTitle);
	}
	
	private void setupUserControls(){
		if (Utility.mFacebook == null  || !Utility.mFacebook.isSessionValid()){
			ImageButton shareBtn = (ImageButton) findViewById(R.id.shareImageButton);
			shareBtn.setEnabled(false);
			
			RelativeLayout diningHistoryLayout = (RelativeLayout) findViewById(R.id.diningHistorybarrelativeLayout);
			diningHistoryLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	void setDishImageView(Bitmap bmp){
		ImageView recipeImageView = (ImageView) findViewById(R.id.recipeImageView);
		if (bmp != null)
			recipeImageView.setImageBitmap(bmp);
	}
	
	public void onShareBtnClick(View sender){
		Bundle params = new Bundle();
        params.putString("caption", dtouchMarker.getTitle());
        params.putString("description", "Busaba");
        if (dtouchMarker.getCodeKey() != null)
        	params.putString("picture", DtouchMarkerWebServicesURL.getMarkerThumbnailURL(dtouchMarker.getCodeKey()).toString());
        else
        	params.putString("picture", DtouchMarkerWebServicesURL.getDishThumbnailURL(dtouchMarker.getTitle()).toString());
        Utility.mFacebook.dialog(TWDishActivity.this, "feed", params, new FacebookPostDialogListener(this, new Handler()));
	}
	
	public void onRecipeBtnClick(View sender){
		Bundle bundle = DtouchMarker.createMarkerBundle(dtouchMarker);
		bundle.putString("URLToDisplay", dtouchMarker.getURL1());
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public void onStoryBtnClick(View sender){
		Bundle bundle = DtouchMarker.createMarkerBundle(dtouchMarker);
		bundle.putString("URLToDisplay", dtouchMarker.getURL2());
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public void onDiningHistoryBtnClick(View sender){
		displayDiningHistoryListActivity();
	}
	
    void getPersonMarkerDetail(String code, String title, String userId){
    	showSpinner();
    	DtouchMarkerDataWebServices dtouchMarkerWebServices = new DtouchMarkerDataWebServices(new MarkerDownloadRequestListener(){
    		public void onMarkerDownloaded(DtouchMarker marker){
    			markerPersonDetailDownloaded(marker);
    		}

			@Override
			public void onMarkerDownloadError() {
				markerPersonDetailDownloadError();			
			}
    	});
    	if (code != null)
    		dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, userId);
    	else if (title != null)
    		dtouchMarkerWebServices.executeDishRequestUsingDishName(title, userId);
    }
    
    private void markerPersonDetailDownloaded(DtouchMarker marker){
    	dtouchMarker = marker;
    	hideSpinner();
    	ImageButton accessoryBtn = (ImageButton) findViewById(R.id.dininghistoryaccessorybtn);
    	if (dtouchMarker.getDiningHistory() != null)
    		accessoryBtn.setEnabled(true);
    	else
    		accessoryBtn.setEnabled(false);
    	if (dtouchMarker.getCodeKey() != null)
    		getMarkerImage(dtouchMarker.getCodeKey());
    	else if (dtouchMarker.getTitle() != null)
    		getDishImage(dtouchMarker.getTitle());
    		
    }
    
    private void markerPersonDetailDownloadError(){
    	hideSpinner();
    	MessageDialog.showMessage(R.string.marker_download_error, this);
    }
	
	/*spinner functions.*/
	void showSpinner(){
		if (mSpinner != null)
			mSpinner.show();
	}
	
	void hideSpinner(){
		if (mSpinner != null)
			mSpinner.dismiss();
	}
	
	private void setupSpinner(){
		mSpinner = new ProgressDialog(this);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
	}
	
	
	private void displayDiningHistoryListActivity(){
		Intent intent = new Intent(this, TWDiningHistoryListActivity.class);
		intent.putParcelableArrayListExtra(getString(R.string.dining_history), (ArrayList<? extends Parcelable>) dtouchMarker.getDiningHistory());
		startActivity(intent);
	}
	
	
	
}
