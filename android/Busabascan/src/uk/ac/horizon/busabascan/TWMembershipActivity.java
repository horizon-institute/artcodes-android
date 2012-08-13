package uk.ac.horizon.busabascan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.FavouriteDishThumbnailsWebService;
import uk.ac.horizon.data.TWMember;
import uk.ac.horizon.data.TWUserDataWebServices;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.data.FavouriteDishThumbnailsWebService.FavouriteDishThumbnailsDownloadRequestListener;
import uk.ac.horizon.data.TWUserDataWebServices.UserDataDownloadRequestListener;

import com.example.coverflow.CoverFlow;
import com.example.coverflow.CoverFlowImageAdapter;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TWMembershipActivity extends Activity{
	private Handler mHandler;
	private TextView mMemberName;
	private ImageView mMemberPhoto;
	private TWFacebookUser mMember;
	private String mSelectedDish;
	ProgressDialog mSpinner;
	Gallery mGallery;
	CoverFlow mCoverFlow;
	HashMap<String, Bitmap> mFavThumbnails;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membership);
		setupSpinner();
		initViewControls();
	}
	
	private void initViewControls(){
		mHandler = new Handler();
		mMemberName = (TextView) findViewById(R.id.membername);
		mMemberPhoto = (ImageView) findViewById(R.id.memberphoto);
		mMember = new TWFacebookUser();
		displayFacebookUserData();
	}
	
	private void displayFacebookUserData(){
		if (Utility.mFacebook.isSessionValid()){
			//Retrieve data from the device if available.
			try{
			if (restoreMemberData()){
				updateControls();
				downloadMemberData();
			}else{
				//otherwise get it from the internet.
				mSpinner.show();
				requestMemberData();
			}
			}catch(FileNotFoundException e){
				MessageDialog.showMessage(R.string.member_data_retrieve_error, this);
			}
		}
	}
	
	void setupCoverFlow(){
		mCoverFlow = (CoverFlow) findViewById(R.id.coverFlow);
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(mFavThumbnails.values());
		mCoverFlow.setAdapter(new CoverFlowImageAdapter(bitmaps, this));
        mCoverFlow.setSpacing(-10);
        int position = (int)bitmaps.size()/2;
        mCoverFlow.setSelection((int)bitmaps.size()/2, true);
        mSelectedDish = getSelectedDish(position);
        displaySelectedDishLayout();
        displaySelectedDishDetail();
        mCoverFlow.setAnimationDuration(1000);
        setupCoverFlowItemListener(mCoverFlow);
    }
	
    private void setupCoverFlowItemListener(CoverFlow coverFlow){
    	coverFlow.setOnItemSelectedListener(new OnItemSelectedListener(){

    		@Override
			public void onItemSelected(AdapterView<?> gallery, View galleryItem,
					int position, long id) {
    			mSelectedDish = getSelectedDish(position);
    			displaySelectedDishDetail();
			}

			@Override
			public void onNothingSelected(AdapterView<?> coverFlow) {
				//get the first item.
				mSelectedDish =  mFavThumbnails.entrySet().iterator().next().getKey();
				displaySelectedDishDetail();
			}
			
		});
	}
    
    private void downloadMemberData(){
    	TWUserDataWebServices userDataWebService = new TWUserDataWebServices(new UserDataDownloadRequestListener(){
    		public void onUserDataDownloaded(TWMember member){
    			setupOffersList(member.offers);
    			downloadFavouriteDishes(member.favouriteDishNames);
    		}

			@Override
			public void onUserDataDownloadError() {
				MessageDialog.showMessage(R.string.member_data_download_error, TWMembershipActivity.this);
			}
    	});
    	userDataWebService.executeUserDataRequest(Utility.userUID);
    }
    
	void downloadFavouriteDishes(List<String> favouriteDishNames){
		FavouriteDishThumbnailsWebService thumbnailServices = 
				new FavouriteDishThumbnailsWebService(new FavouriteDishThumbnailsDownloadRequestListener(){
					public void onFavouriteDishThumbnailsDownloaded(HashMap<String,Bitmap> thumbnailsMap){
						mFavThumbnails = thumbnailsMap;
						hideProgressControls();
						if (mFavThumbnails != null && mFavThumbnails.size() > 0)
							setupCoverFlow();
					}
				});
		showProgressControls();
		thumbnailServices.executeFavouriteDishRequest((String[])favouriteDishNames.toArray());
	}

	private void setupSpinner(){
		mSpinner = new ProgressDialog(this);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
	}

	void showProgress(){
		if (mSpinner != null)
			mSpinner.show();
	}
	
	void hideProgress(){
		if (mSpinner != null)
			mSpinner.dismiss();
	}
	
	private void setupOffersList(List<String> offers){
		ListView offerList = (ListView) findViewById(R.id.memberoffersList);
		offerList.setAdapter(new ArrayAdapter<String>(this,R.layout.offer_list_item,offers));
	}
	
	/*
	 * Request member name, and picture to show on the main screen.
	 */
	private void requestMemberData() {
		Bundle params = new Bundle();
		params.putString("fields", "id, name, picture");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
	}
	
	private boolean restoreMemberData() throws FileNotFoundException{
		return mMember.restoreMember(this);
	}
	
	
	private void requestMemberPicture(){
		new GetProfilePictureAsyncTask().execute(Utility.userUID);
	}
	
	void showProgressControls(){
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.coverFlowframeLayout);
		LayoutInflater inflater = getLayoutInflater();
		inflater.inflate(R.layout.scanprogress, frameLayout);
	}

	void hideProgressControls(){
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.coverFlowframeLayout);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.scanProgressBar);
		frameLayout.removeView(progressBar);
		progressBar = null;
	}
	
	 String getSelectedDish(int position){
		int count = 0;
		String selectedDish = null;
		for (Entry<String,Bitmap> entry: mFavThumbnails.entrySet()){
			if (count == position){
				selectedDish = entry.getKey();
				break;
			}
			count++;
		}
		return selectedDish; 
	}
	 
	void displaySelectedDishLayout(){
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.favouriteDishBarWithAccessoryBtn);
		layout.setVisibility(View.VISIBLE);
	}
	
	void displaySelectedDishDetail(){
		TextView selectedDishTextView = (TextView) findViewById(R.id.favouriteSelectedDishTitle);
		selectedDishTextView.setText(mSelectedDish);
	}
	
	public void onDishDetailBtnClick(View sender){
		if (mSelectedDish != null){
			showProgress();
			getMarker(mSelectedDish);
		}
	}
	
	void getMarker(String dishName){
		DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener(){
			public void onMarkerDownloaded(DataMarker marker){
				hideProgress();
				displayDish(marker);
			}

			@Override
			public void onMarkerDownloadError() {
				hideProgress();
				MessageDialog.showMessage(R.string.dish_download_error, TWMembershipActivity.this);
			}
		});
		dtouchMarkerWebServices.executeDishRequestUsingDishName(dishName, Utility.userUID);
	}
	
	private void displayDish(DataMarker marker){
		Intent intent = new Intent(this, TWOldDishActivity.class);
		Bundle markerBundle = DataMarker.createMarkerBundle(marker);
		intent.putExtras(markerBundle);
		startActivity(intent);
	}
	
	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
    class UserRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                final String picURL = jsonObject.getString("picture");
                Utility.userUID = jsonObject.getString("id");
                Utility.userName = jsonObject.getString("name");
                
                mHandler.post(new Runnable() {
                	
                	@Override
                    public void run() {
                    	mMember.name = Utility.userName;
                    	mMember.id = Utility.userUID;
                    	mMember.bitmapType = getImageUrlType(picURL);
                    	try {
							mMember.setPicture(Utility.getBitmap(picURL));
						} catch (IOException e) {
							mMember.setPicture(null);
							MessageDialog.showMessage(R.string.pic_download_error, TWMembershipActivity.this);
						}
                        try {
							mMember.saveMember(TWMembershipActivity.this);
						} catch (IOException e) {
							MessageDialog.showMessage(R.string.member_data_save_error, TWMembershipActivity.this);
						}
                        updateControls();
                        TWMembershipActivity.this.requestMemberPicture();
                    }
                });
                hideProgress();

            } catch (JSONException e) {
                MessageDialog.showMessage(R.string.facebookErrMsg, TWMembershipActivity.this);
            }
        }
        
        @Override
        public void onFacebookError(FacebookError e, final Object state) {
            super.onFacebookError(e, state);
            hideProgress();
        }

        @Override
        public void onFileNotFoundException(FileNotFoundException e, final Object state) {
            Log.e("Facebook", e.getMessage());
            MessageDialog.showMessage(R.string.facebookErrMsg, TWMembershipActivity.this);
            hideProgress();
        }

        @Override
        public void onIOException(IOException e, final Object state) {
            Log.e("Facebook", e.getMessage());
            MessageDialog.showMessage(R.string.facebookErrMsg, TWMembershipActivity.this);
            hideProgress();
        }

        @Override
        public void onMalformedURLException(MalformedURLException e, final Object state) {
            Log.e("Facebook", e.getMessage());
            MessageDialog.showMessage(R.string.facebookErrMsg, TWMembershipActivity.this);
            hideProgress();
        }

    }
    
    private String getImageUrlType(String url){
    	Uri.Builder uriBuilder = new Uri.Builder();
    	uriBuilder.appendPath(url);
    	Uri picUri = uriBuilder.build();
    	String urlType = TWMembershipActivity.this.getContentResolver().getType(picUri);
    	if (urlType == null){
    		String fileName = picUri.getLastPathSegment();
    		int fileExtIndex = fileName.lastIndexOf(".");
    		// if extension found
    		if (fileExtIndex != -1){
    			String fileExt = fileName.substring(fileExtIndex+1);
    			urlType = "image/" + fileExt;
    		}else{
    			urlType = "";
    		}
    			
    	}
    	return urlType;
    }
    
    private void updateControls(){
    	mMemberName.setText("Welcome " + mMember.name + "!");
    	updatePhoto();
    }
    
    private void updatePhoto(){
    	mMemberPhoto.setImageBitmap(null);
    	mMemberPhoto.setImageBitmap(mMember.getPicture());
    }
    
    /*
     * Start a AsyncTask to fetch the request
     */
    private class GetProfilePictureAsyncTask extends AsyncTask<String, Void, Bitmap> {
    	@Override
    	protected Bitmap doInBackground(String... objectIDs) {
    		Bitmap bmp = null;
    		String fbUrl = null;
    		fbUrl = new String("http://graph.facebook.com/"+ objectIDs[0] + "/picture?type=normal");
    		try {
				bmp = Utility.getBitmap(fbUrl);
			} catch (IOException e) {
				bmp = null;
			}
    		return bmp;
    	}

    	@Override
    	protected void onPostExecute(Bitmap bmp) {
    		if (bmp != null){
    			mMember.setPicture(bmp);
    			updatePhoto();
    			try {
					mMember.saveMemberPhoto(TWMembershipActivity.this);
				} catch (IOException e) {
					MessageDialog.showMessage(R.string.member_pic_save_error, TWMembershipActivity.this);
				}
    		}else{
    			MessageDialog.showMessage(R.string.pic_download_error, TWMembershipActivity.this);
    		}
    		downloadMemberData();
    	}
    }
    
}
