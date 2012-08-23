package uk.ac.horizon.busabascan;

import uk.ac.horizon.busabascan.TWOldMainActivity.FbAPIsLogoutListener;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Utility;
import com.facebook.android.SessionEvents.LogoutListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;

public class LogoutFragment extends DialogFragment {

	private static final int LOGOUT_ITEM = 0;
	private static final int CANCEL_ITEM = 1;

	public LogoutFragment(Activity parent) {
		activity = parent;
	}

	static LogoutFragment newInstance(Activity parent){
		LogoutFragment frag = new LogoutFragment(parent);
		return frag;
	}

	private Context context;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
		context = activity.getBaseContext();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.settings_title)
		.setItems(R.array.settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case LOGOUT_ITEM:
					onSettingsLogoutItemClick(LogoutFragment.this);
					break;
				case CANCEL_ITEM:
					onSettingsCancelItemClick(LogoutFragment.this);
					break;
				default:
					break;
				}
			}

		})
		.create();
	}
	
    public class FbAPIsLogoutListener implements LogoutListener {

		@Override
        public void onLogoutBegin() {
            //mText.setText("Logging out...");
        }

        @Override
        public void onLogoutFinish() {
			//SessionStore.clear(TWOldMainActivity.this);
        	new TWFacebookUser().removeMember(context);
        	if (Utility.mFacebook != null)
        		Utility.mFacebook = null;
        	//setupUserControls();
        	//hideSpinner();
        }
    }
	
	
    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {          
        	SessionEvents.onLogoutFinish();          
        }
    }

	
	/*
	 * Logoutfragment call back functions.
	 */
    public void onSettingsLogoutItemClick(LogoutFragment frag){
    	//showSpinner();
    	frag.dismiss();
        if (Utility.mFacebook != null && Utility.mFacebook.isSessionValid()) {
            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
            asyncRunner.logout(context, new LogoutRequestListener());
        }
    }
    
    public void onSettingsCancelItemClick(LogoutFragment frag){
    	frag.dismiss();
    }
}


