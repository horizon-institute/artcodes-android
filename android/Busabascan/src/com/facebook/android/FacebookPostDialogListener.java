package com.facebook.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

/*
 * Callback after the message has been posted on friend's wall.
 */
public class FacebookPostDialogListener extends BaseDialogListener {
    private Handler mHandler;
    private Context mContext;
	
    public FacebookPostDialogListener(Context context, Handler handler){
    	mHandler = handler;
    	mContext = context;
    }
        
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
        Toast.makeText(mContext.getApplicationContext(), "Facebook Error: " + error.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel() {
        Toast toast = Toast.makeText(mContext.getApplicationContext(), "Update status cancelled",
                Toast.LENGTH_SHORT);
        toast.show();
    }
    
    public void showToast(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}