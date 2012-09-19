package uk.ac.horizon.busabascan;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class OrderOnClick extends Activity implements OnClickListener {
	
    private Activity activity;
	private String dish_name;
	
	OrderOnClick(Activity act, String d)
	{
		super();
	    activity = act;
	    dish_name = d;
	}

	@Override
	public void onClick(View v) {
		Comunique c = new Comunique(v);
		String msg = "Order " + dish_name;
		String title = "Order " + dish_name + " now?";
		c.send(msg, title);
	}

}
