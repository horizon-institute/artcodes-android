package uk.ac.horizon.aestheticodes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class TWSplashScreenActivity extends FragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// displaySplashScreen();
		setContentView(R.layout.splashscreen);

		new Handler().postDelayed(new Runnable()
		{

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run()
			{
				// This method will be executed once the timer is over
				// Start your app main activity
				Intent i = new Intent(TWSplashScreenActivity.this, TabControl.class);
				startActivity(i);

				// close this activity
				finish();
			}
		}, 3000);
	}

	void displayMainActivity()
	{
		// Intent intent = new Intent(this.getApplicationContext(),
		// TWMainActivity.class);
		Intent intent = new Intent(this.getApplicationContext(), TabControl.class);
		startActivity(intent);
		this.finish();
	}

	private void displaySplashScreen()
	{
		final TWSplashScreenFragment frag = TWSplashScreenFragment.newInstance();
		frag.show(this.getSupportFragmentManager(), "SPLASH_SCREEN");
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			public void run()
			{
				frag.dismiss();
				displayMainActivity();
			}
		}, 1000);
	}
}
