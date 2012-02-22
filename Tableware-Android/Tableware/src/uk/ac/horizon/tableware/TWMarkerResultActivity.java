package uk.ac.horizon.tableware;

import uk.ac.horizon.dtouch.DtouchMarker;
import uk.ac.horizon.dtouch.DtouchMarkersDataSource;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TWMarkerResultActivity extends Activity {
	
	private DtouchMarker mDtouchMarker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.markeridentificationresult);
        initMarker();
        initTextViews();
        initActivityTitle();
    }
	
	private void initMarker(){
		Intent intent = getIntent();
		Bundle markerBundle = intent.getExtras();
		DtouchMarker markerFromBundle = DtouchMarker.createMarkerFromBundle(markerBundle);
		DtouchMarker markerFromDataSource = DtouchMarkersDataSource.getDtouchMarkerUsingKey(markerFromBundle.getCodeKey());
		if (markerFromDataSource != null)
			mDtouchMarker = markerFromDataSource;
		else
			mDtouchMarker = markerFromBundle;
	}
	
	private void initTextViews(){
		if (mDtouchMarker!= null){
			setMarkerCode();
			setMarkerDescription();
		}
	}
	
	private void setMarkerCode(){
		TextView markerCodeTextView = (TextView) findViewById(R.id.markerCode); 
		markerCodeTextView.setText(mDtouchMarker.getCodeKey());
	}
	
	private void setMarkerDescription(){
		TextView markerDescTextView = (TextView) findViewById(R.id.markerDescription);
		if (mDtouchMarker.getDescription() != null)
			markerDescTextView.setText(mDtouchMarker.getDescription());
		else
			markerDescTextView.setText(R.string.marker_desc_undefined);
	}
	
	public void displayMarkerInformation(View view){
		this.finish();
		Intent intent = new Intent(this, TWBrowseMarkerActivity.class);
		Bundle markerBundle = DtouchMarker.createMarkerBundleFromCode(mDtouchMarker);
		intent.putExtras(markerBundle);
		startActivity(intent);
	}
	
	public void closeActivity(View view){
		this.finish();
	}
	
	private void initActivityTitle(){
		if (mDtouchMarker != null && mDtouchMarker.getDescription() != null)
			this.setTitle(mDtouchMarker.getDescription());
	}
}
