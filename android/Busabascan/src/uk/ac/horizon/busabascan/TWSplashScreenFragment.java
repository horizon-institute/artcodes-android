package uk.ac.horizon.busabascan;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TWSplashScreenFragment extends DialogFragment {
	
	static TWSplashScreenFragment newInstance(){
		TWSplashScreenFragment frag = new TWSplashScreenFragment();
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.setStyle(STYLE_NO_TITLE, getTheme());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.splashscreen, container, false);
		return view;
	}
}
