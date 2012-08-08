package uk.ac.horizon.busabascan;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.facebook.android.BaseRequestListener;
import com.facebook.android.Utility;

public class TWOutsideActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outside);
		new Handler();
		ExpandableListAdapter mAdapter;
	    ExpandableListView epView = (ExpandableListView) findViewById(R.id.expandableListView1);
	    mAdapter = new RestaurantListAdapter(this);
	    epView.setAdapter(mAdapter);
	}
	
	/* Called when busaba menu button clicked */
	public void gotoMenu(View view) {
		// Start the new activity
		Intent intent = new Intent(this, TWMenuActivity.class);
		startActivity(intent);
	}
	
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
				MessageDialog.showMessage(R.string.facebookErrMsg, TWOutsideActivity.this);
			}finally{
				//hideSpinner();
			}
		}
	}
	    
}
  