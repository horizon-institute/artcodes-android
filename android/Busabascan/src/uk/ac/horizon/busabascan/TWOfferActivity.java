package uk.ac.horizon.busabascan;

import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerImageWebServices;
import uk.ac.horizon.data.DataMarkerWebServicesURL;
import uk.ac.horizon.data.DataMarkerImageWebServices.MarkerImageDownloadRequestListener;

import com.facebook.android.BaseDialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Utility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TWOfferActivity extends Activity {
	private DataMarker dtouchMarker;
	private Handler mHandler;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offer);
		initMarker();
		mHandler = new Handler();
		setActivityCaption();
		getOfferImage(dtouchMarker.getCode());
	}
	
	private void initMarker(){
		Intent intent = getIntent();
		Bundle markerBundle = intent.getExtras();
		dtouchMarker = DataMarker.createMarkerFromBundle(markerBundle);
	}
	
    void showProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.offerImageframeLayout);
    	LayoutInflater inflater = getLayoutInflater();
    	inflater.inflate(R.layout.scanprogress, frameLayout);
    }
    
    void hideProgressControls(){
    	FrameLayout frameLayout = (FrameLayout) findViewById(R.id.offerImageframeLayout);
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
	
	private void getOfferImage(String markerCode){
		DataMarkerImageWebServices dtouchMarkerImageWebServices = new DataMarkerImageWebServices(new MarkerImageDownloadRequestListener(){
			public void onMarkerImageDownloaded(Bitmap bmp){
				hideProgressControls();
				setOfferImageView(bmp);
			}

			@Override
			public void onMarkerImageDownloadError() {
				hideProgressControls();
				MessageDialog.showMessage(R.string.offer_image_download_error, TWOfferActivity.this);
			}
		});
		showProgressControls();
		dtouchMarkerImageWebServices.executeMarkerImageRequest(markerCode);
	}
	
	void setOfferImageView(Bitmap bmp){
		ImageView offerImageView = (ImageView) findViewById(R.id.offerImageView);
		if (bmp != null)
			offerImageView.setImageBitmap(bmp);
	}
	
	public void onShareBtnClick(View sender){
		Bundle params = new Bundle();
        params.putString("caption", dtouchMarker.getTitle());
        params.putString("description", "Busaba");
        params.putString("picture", DataMarkerWebServicesURL.getMarkerThumbnailURL(dtouchMarker.getCode()).toString());
        Utility.mFacebook.dialog(TWOfferActivity.this, "feed", params, new FacebookPostDialogListener());
	}
	
	public void onOfferDetailBtnClick(View sender){
		Bundle bundle = DataMarker.createMarkerBundle(dtouchMarker);
		bundle.putString("URLToDisplay", dtouchMarker.getURL1());
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
    /*
     * Callback after the message has been posted on friend's wall.
     */
    public class FacebookPostDialogListener extends BaseDialogListener {
        @Override
        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                showToast("Message posted on the wall.");
            } else {
                showToast("No message posted on the wall.");
            }
        }
       
        @Override
        public void onFacebookError(FacebookError error) {
            Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast toast = Toast.makeText(getApplicationContext(), "Update status cancelled",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void showToast(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(TWOfferActivity.this, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
