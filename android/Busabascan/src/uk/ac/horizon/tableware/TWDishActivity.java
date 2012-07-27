package uk.ac.horizon.tableware;

import java.util.ArrayList;
import com.facebook.android.FacebookPostDialogListener;
import com.facebook.android.R;
import com.facebook.android.Utility;

import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerImageWebServices;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.DataMarkerWebServicesURL;
import uk.ac.horizon.data.DataMarkerImageWebServices.MarkerImageDownloadRequestListener;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
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
	private DataMarker mDataMarker;
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
			getPersonMarkerDetail(mDataMarker.getCode(), mDataMarker.getTitle(), Utility.userUID);
		else
			getPersonMarkerDetail(mDataMarker.getCode(), mDataMarker.getTitle(), null);
	}
	
	private void initMarker(){
		Intent intent = getIntent();
		Bundle markerBundle = intent.getExtras();
		mDataMarker = DataMarker.createMarkerFromBundle(markerBundle);
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
		if (mDataMarker != null){
			activityCaptionTextView.setText(mDataMarker.getTitle());
		}
	}
	
	private void getMarkerImage(String markerCode){
		DataMarkerImageWebServices dtouchMarkerImageWebServices = new DataMarkerImageWebServices(new MarkerImageDownloadRequestListener(){
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
		DataMarkerImageWebServices dtouchMarkerImageWebServices = new DataMarkerImageWebServices(new MarkerImageDownloadRequestListener(){
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
        params.putString("caption", mDataMarker.getTitle());
        params.putString("description", "Busaba");
        if (mDataMarker.getCode() != null)
        	params.putString("picture", DataMarkerWebServicesURL.getMarkerThumbnailURL(mDataMarker.getCode()).toString());
        else
        	params.putString("picture", DataMarkerWebServicesURL.getDishThumbnailURL(mDataMarker.getTitle()).toString());
        Utility.mFacebook.dialog(TWDishActivity.this, "feed", params, new FacebookPostDialogListener(this, new Handler()));
	}
	
	public void onRecipeBtnClick(View sender){
		Bundle bundle = DataMarker.createMarkerBundle(mDataMarker);
		bundle.putString("URLToDisplay", mDataMarker.getURL1());
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public void onStoryBtnClick(View sender){
		Bundle bundle = DataMarker.createMarkerBundle(mDataMarker);
		bundle.putString("URLToDisplay", mDataMarker.getURL2());
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public void onDiningHistoryBtnClick(View sender){
		displayDiningHistoryListActivity();
	}
	
    void getPersonMarkerDetail(String code, String title, String userId){
    	showSpinner();
    	DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener(){
    		public void onMarkerDownloaded(DataMarker marker){
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
    
    private void markerPersonDetailDownloaded(DataMarker marker){
    	mDataMarker = marker;
    	hideSpinner();
    	ImageButton accessoryBtn = (ImageButton) findViewById(R.id.dininghistoryaccessorybtn);
    	if (mDataMarker.getDiningHistory() != null)
    		accessoryBtn.setEnabled(true);
    	else
    		accessoryBtn.setEnabled(false);
    	if (mDataMarker.getCode() != null)
    		getMarkerImage(mDataMarker.getCode());
    	else if (mDataMarker.getTitle() != null)
    		getDishImage(mDataMarker.getTitle());
    		
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
		intent.putParcelableArrayListExtra(getString(R.string.dining_history), (ArrayList<? extends Parcelable>) mDataMarker.getDiningHistory());
		startActivity(intent);
	}
	
	
	
}
