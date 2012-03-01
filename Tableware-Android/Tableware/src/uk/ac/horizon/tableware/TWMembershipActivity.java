package uk.ac.horizon.tableware;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.BaseRequestListener;
import com.facebook.android.Utility;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class TWMembershipActivity extends Activity {
	private Handler mHandler;
	private TextView mMemberName;
	private ImageView mMemberPhoto;
	private TWMember mMember;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.membership);
		mHandler = new Handler();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mMemberName = (TextView) findViewById(R.id.membername);
		mMemberPhoto = (ImageView) findViewById(R.id.memberphoto);
		mMember = new TWMember();
		displayMembershipData();
	}
	
	private void displayMembershipData(){
		if (Utility.mFacebook.isSessionValid()){
			//Retrieve data from the device if available.
			if (restoreMemberData()){
				updateControls();
			}else{
				//otherwise get it from internet.
				requestMemberData();
			}
		}
	}
	
	/*
	 * Request member name, and picture to show on the main screen.
	 */
	private void requestMemberData() {
		Bundle params = new Bundle();
		params.putString("fields", "id, name, picture");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
	}
	
	private boolean restoreMemberData(){
		return mMember.restoreMember(this);
	}
	
	
	private void requestMemberPicture(){
		new GetProfilePictureAsyncTask().execute(Utility.userUID);
	}
	
	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
    public class UserRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                final String picURL = jsonObject.getString("picture");
                final String name = jsonObject.getString("name");
                Utility.userUID = jsonObject.getString("id");
                
                mHandler.post(new Runnable() {
                	
                	@Override
                    public void run() {
                    	mMember.name = name;
                    	mMember.id = Utility.userUID;
                    	mMember.bitmapType = getImageUrlType(picURL);
                    	mMember.setPicture(Utility.getBitmap(picURL));
                        mMember.saveMember(TWMembershipActivity.this);
                        updateControls();
                        TWMembershipActivity.this.requestMemberPicture();
                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
    		String fbUrl = null;
    		fbUrl = new String("http://graph.facebook.com/"+ objectIDs[0] + "/picture?type=normal");
    		return Utility.getBitmap(fbUrl);
    	}

    	@Override
    	protected void onPostExecute(Bitmap result) {
    		mMember.setPicture(result);
    		updatePhoto();
    		mMember.saveMemberPhoto(TWMembershipActivity.this);
    	}
    }
    
}
