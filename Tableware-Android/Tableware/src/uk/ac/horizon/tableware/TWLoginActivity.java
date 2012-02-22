package uk.ac.horizon.tableware;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.LoginButton;
import com.facebook.android.R;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Utility;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TWLoginActivity extends Activity {
	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
	String[] permissions = {"publish_stream"};
	private LoginButton mLoginButton;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initFacebook();
		initFacebookLoginButton();
		/*
        if (Utility.mFacebook.isSessionValid()) {
            requestUserData();
        }*/

	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (Utility.mFacebook != null){
			if (!Utility.mFacebook.isSessionValid()){
				//logout
			}else{
				Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
				//display membership data.
				displayMainActivity();
				this.finish();
			}
			
		}
	}
	
	private void displayMainActivity(){
		Intent intent = new Intent(this.getApplicationContext(), TWMainActivity.class);
		startActivity(intent);
	}
	
 	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		/*
		 * if this is the activity result from authorization flow, do a call
		 * back to authorizeCallback Source Tag: login_tag
		 */
		case AUTHORIZE_ACTIVITY_RESULT_CODE: {
			Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
			break;
		}

		}
	}
	
	private void initFacebook(){
		// Create the Facebook Object using the app id.
		Utility.mFacebook = Utility.getAppFacebookObj();
		 // Instantiate the asynrunner object for asynchronous api calls.
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
        // restore session if one exists
        SessionStore.restore(Utility.mFacebook, this.getApplicationContext());
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
	}
	
	private void initFacebookLoginButton(){
		mLoginButton = (LoginButton) findViewById(R.id.facebookloginBtn);
		mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);
	}
	
    /*
     * The Callback for notifying the application when authorization succeeds or
     * fails.
     */

    public class FbAPIsAuthListener implements AuthListener {

        @Override
        public void onAuthSucceed() {
            //requestUserData();
        }

        @Override
        public void onAuthFail(String error) {
            //mText.setText("Login Failed: " + error);
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
            //mText.setText("You have logged out! ");
            //mUserPic.setImageBitmap(null);
        }
    }
    
}
