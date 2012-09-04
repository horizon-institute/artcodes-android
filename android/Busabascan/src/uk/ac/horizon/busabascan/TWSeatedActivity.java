package uk.ac.horizon.busabascan;

import uk.ac.horizon.data.HIPreferenceTableware;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class TWSeatedActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twseated);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twseated, menu);
        return true;
    }
    
	public void kitchenOnClick(View view) {
		// Start the camera
		HIPreferenceTableware preference = new HIPreferenceTableware(this);
		String host = preference.getKitchenIP();
		String url = "rtsp://"+ host + "/kitchen";
    	//String url = "rtsp://192.168.0.8/kitchen";
		Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse(url));
		startActivity(intent);
	}
	public void callOnClick(View view) {
		Comunique c = new Comunique(view);
		String msg = "Waiter please";
		String title = "Call a waiter?";
		c.send(msg, title);
	}
	public void aboutOnClick(View view) {
		// Start the new activity
		Intent intent = new Intent(this, TWMenuActivity.class);
		startActivity(intent);
	}
	public void musicOnClick(View view) {
		// Start the new activity
		Intent intent = new Intent(this, TWMenuActivity.class);
		startActivity(intent);
	}
	public void payOnClick(View view) {
		Comunique c = new Comunique(view);
		String msg = "Request bill";
		String title = "Request your bill now?";
		c.send(msg, title);
	}

}
