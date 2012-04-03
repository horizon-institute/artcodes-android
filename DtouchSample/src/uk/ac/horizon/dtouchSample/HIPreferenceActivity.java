package uk.ac.horizon.dtouchSample;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HIPreferenceActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}
	
}
