package uk.ac.horizon.tableware;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class TWMainActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
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
		
	}
	
	public void onSpecialOffersBtnClick(View sender){
		
	}
}
