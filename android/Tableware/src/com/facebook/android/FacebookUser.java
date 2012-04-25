package com.facebook.android;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;

public class FacebookUser {
	/*
	 * Request user facebook id and  name.
	 */
	public void requestUserIdAndName() {
		Bundle params = new Bundle();
		params.putString("fields", "id, name");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


