package uk.ac.horizon.tableware;

import java.net.URI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class TWBrowseMarkerActivity extends Activity {
	
	private WebView mWebView;
	private DtouchMarker mDtouchMarker; 
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browsemarkeresult);
        initDtouchMarker();
        initWebView();
        initActivityTitle();
    }
	
	private void initDtouchMarker(){
		Intent intent = this.getIntent();
		Bundle markerBundle = intent.getExtras();
		DtouchMarker bundleMarker = DtouchMarker.createMarkerFromBundle(markerBundle);
		DtouchMarker dataSourceMarker = DtouchMarkersDataSource.getDtouchMarkerUsingKey(bundleMarker.getCodeKey());
		if (dataSourceMarker != null)
			mDtouchMarker = dataSourceMarker;
		else
			mDtouchMarker = dataSourceMarker;
	}
	
	private void initWebView(){
		if (mDtouchMarker != null && mDtouchMarker.getClass() != null){
			mWebView = (WebView) findViewById(R.id.MarkerwebView);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			String encodedURL = appendMemberNameWithURL(mDtouchMarker.getURL());
			if (encodedURL != null)
				mWebView.loadUrl(encodedURL);
		}
	}
	
	private void initActivityTitle(){
		if (mDtouchMarker != null && mDtouchMarker.getDescription() != null)
			this.setTitle(mDtouchMarker.getDescription());
	}
	
	private String appendMemberNameWithURL(String url){
		String encodedURL = null; 
		String memberName = new TWPreference(this).getMemberName();
		try{
			url = url.concat(memberName.replaceAll(" ", "%20"));
			URI uri = new URI(url);
			encodedURL = uri.toString();
		}
		catch(Exception e){
			encodedURL = null;
		}
		return encodedURL;
	}
	
}
