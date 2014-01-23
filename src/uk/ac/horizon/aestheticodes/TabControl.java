package uk.ac.horizon.aestheticodes;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;

public class TabControl extends TabActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		setContentView(R.layout.new_main);

		// Tab Control
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		Intent intent;

		// Scan Mode
		intent = new Intent().setClass(this.getApplicationContext(), TWCameraActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		spec = tabHost.newTabSpec("Scan").setIndicator("", res.getDrawable(R.drawable.scandark)).setContent(intent);

		tabHost.addTab(spec);

		// Debug Mode
		// Kill current activity when switching to tab

		// intent = new Intent().setClass(this.getApplicationContext(),
		// TWDebugModeView.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// spec = tabHost.newTabSpec("Drawing").setIndicator("Drawing",
		// res.getDrawable(R.drawable.samplesbtn)).setContent(intent);
		// tabHost.addTab(spec);

		// Settings
		intent = new Intent().setClass(this, TWPreferenceActivity.class);
		spec = tabHost.newTabSpec("Settomgs").setIndicator("", res.getDrawable(R.drawable.settings_button)).setContent(intent);
		tabHost.addTab(spec);

		// Set Tab Width
		// tabHost.getTabWidget().getChildAt(0).getLayoutParams().width = 40;
		// tabHost.getTabWidget().getChildAt(1).getLayoutParams().width=40;
		// tabHost.getTabWidget().getChildAt(2).getLayoutParams().width=40;
		// tabHost.getTabWidget().getChildAt(3).getLayoutParams().width=40;

		// get drawable resource id
		// int myDrawable = R.drawable.menubar;

		// Set background colour of the tabs
		//
		tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#E0D4B7"));
		tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.parseColor("#E0D4B7"));
		// tabHost.getTabWidget().getChildAt(2).setBackgroundResource(myDrawable);
		// tabHost.getTabWidget().getChildAt(3).setBackgroundResource(myDrawable);

		// set first tab as Today screen (index 0 as first element)
		tabHost.setCurrentTab(0);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
}