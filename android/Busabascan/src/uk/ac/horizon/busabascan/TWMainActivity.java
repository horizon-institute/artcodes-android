package uk.ac.horizon.busabascan;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Utility;
import com.facebook.android.SessionEvents.LogoutListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class TWMainActivity extends FragmentActivity {
	private ProgressDialog mSpinner;
	private Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setupSpinner();
		setContentView(R.layout.main);
		setupUserControls();
		getUserData();
		SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
		mHandler = new Handler();
	}
	
	private void getUserData(){
		if (Utility.mFacebook != null && Utility.mFacebook.isSessionValid()){
			if (Utility.userUID == null){
				showSpinner();
				requestUserIdAndName();
			}
		}
	}
	
	private void setupUserControls(){
		if (Utility.mFacebook == null || !Utility.mFacebook.isSessionValid()){
			ImageButton memberButton = (ImageButton) findViewById(R.id.memberBtn);
			memberButton.setEnabled(false);
			
			ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsBtn);
			settingsButton.setEnabled(false);
		}
	}

	public void onScanBtnClick(View sender){
		Intent intent = new Intent(this, TWCameraActivity.class);
		startActivity(intent);
	}
	
	public void onMemberBtnClick(View sender){
		Intent intent = new Intent(this, TWMembershipActivity.class);
		startActivity(intent);
	}
	
	public void onSettingsBtnClick(View sender){
		displayLogoutFragment();
	}
	
    private void displayLogoutFragment(){
    	final LogoutFragment frag = new LogoutFragment();
    	frag.show(this.getSupportFragmentManager(), "LOGOUT_FRAGMENT");
    }
	
	public void onNewsBtnClick(View sender){
		Intent intent = new Intent(this, TWNewsActivity.class);
		startActivity(intent);
	}
	
	public void requestUserIdAndName() {
		Bundle params = new Bundle();
		params.putString("fields", "id, name");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
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

	/*
	 * Callback for fetching current user's name and uid.
	 */
	class UserRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				Utility.userName = jsonObject.getString("name");
				Utility.userUID = jsonObject.getString("id");
				
			} catch (JSONException e) {
				MessageDialog.showMessage(R.string.facebookErrMsg, TWMainActivity.this);
			}finally{
				hideSpinner();
			}
		}
	}
	
    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {
            /*
             * callback should be run in the original thread, not the background
             * thread
             */
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }
    
    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        @Override
        public void onLogoutBegin() {
            //mText.setText("Logging out...");
        }

        @Override
        public void onLogoutFinish() {
        	SessionStore.clear(TWMainActivity.this);
        	new TWFacebookUser().removeMember(TWMainActivity.this);
        	if (Utility.mFacebook != null)
        		Utility.mFacebook = null;
        	setupUserControls();
        	hideSpinner();
        }
    }
	
	/*
	 * Logoutfragment call back functions.
	 */
    public void onSettingsLogoutItemClick(LogoutFragment frag){
    	showSpinner();
    	frag.dismiss();
        if (Utility.mFacebook != null && Utility.mFacebook.isSessionValid()) {
            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
            asyncRunner.logout(this, new LogoutRequestListener());
        }
    }
    
    public void onSettingsCancelItemClick(LogoutFragment frag){
    	frag.dismiss();
    }
}
