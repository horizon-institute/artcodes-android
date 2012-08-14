package uk.ac.horizon.busabascan;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class DetailOnClick extends Activity implements OnClickListener {
	
    private Activity activity;
	private String dish_name;
	
	DetailOnClick(Activity act, String d)
	{
		super();
	    activity = act;
	    dish_name = d;
	}

	@Override
	public void onClick(View v) {
    	//Intent intent = new Intent(activity.getApplicationContext(), TWOutsideActivity.class);
    	Intent intent = new Intent(activity, TWDishActivity.class);
    	intent.putExtra(TWCameraMainActivity.DISH, dish_name);
    	//Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	activity.startActivity(intent);
	}

}
