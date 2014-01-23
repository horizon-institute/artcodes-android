package uk.ac.horizon.aestheticodes;

import uk.ac.horizon.data.DtouchMarkersDataSource;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class TWPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private MenuItem mResetPrefs;
	private MenuItem mAddPref;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		getListView().setBackgroundColor(Color.TRANSPARENT);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		getListView().setBackgroundColor(Color.parseColor("#E0D4B7"));
		setTheme(R.style.PreferenceScreen);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mResetPrefs = menu.add("Reset prefs to default");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == mResetPrefs)
			resetPrefs();

		return true;
	}

	private void resetPrefs()
	{
		new AlertDialog.Builder(this).setTitle("Reset Preferences?").setMessage("Are you sure you want to reset all preferences to their default value?")
				.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Reset", new DialogInterface.OnClickListener()
				{

					public void onClick(DialogInterface dialog, int whichButton)
					{
						resetToDefault();
					}
				}).setNegativeButton("Cancel", null).show();

	}

	// Clear the prefs and reset to default
	private void resetToDefault()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().clear().commit();
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference, true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// Set flag to reinitialise the markers next time they are scanned
		DtouchMarkersDataSource.prefsChanged = true;
	}

}
