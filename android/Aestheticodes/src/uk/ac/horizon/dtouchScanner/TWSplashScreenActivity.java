package uk.ac.horizon.dtouchScanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class TWSplashScreenActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		displaySplashScreen();
		//setContentView(R.layout.main);
	}
	
	void displayMainActivity(){
		Intent intent = new Intent(this.getApplicationContext(), TWMainActivity.class);
		startActivity(intent);
		this.finish();
	}
	
    private void displaySplashScreen(){
    	final TWSplashScreenFragment frag = TWSplashScreenFragment.newInstance();
    	frag.show(this.getSupportFragmentManager(), "SPLASH_SCREEN");
    	final Handler handler = new Handler();
    	handler.postDelayed(new Runnable(){
    		public void run(){
    			frag.dismiss();
    			displayMainActivity();
    		}
    	}, 1000);
    }
}
