package uk.ac.horizon.aestheticodes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import uk.ac.horizon.aestheticodes.R;

public class TWMainActivity extends FragmentActivity {
	
	private static final String ABOUT_URL = "http://aestheticodes.blogs.wp.horizon.ac.uk/";
	private static final String SAMPLES_URL = "http://aestheticodes.blogs.wp.horizon.ac.uk/designs/";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void onScanBtnClick(View sender){
		Intent intent = new Intent(this, TWCameraActivity.class);
		startActivity(intent);
	}
	
	public void onAboutBtnClick(View sender){
		Bundle urlBundle = new Bundle();
		urlBundle.putString("URL", ABOUT_URL);
		Intent intent = new Intent(this, TWWebsiteActivity.class);
		intent.putExtras(urlBundle);
		startActivity(intent);
	}
	
	public void onSamplesBtnClick(View sender){
		Bundle urlBundle = new Bundle();
		urlBundle.putString("URL", SAMPLES_URL);
		Intent intent = new Intent(this, TWWebsiteActivity.class);
		intent.putExtras(urlBundle);
		startActivity(intent);
	}
	
}
