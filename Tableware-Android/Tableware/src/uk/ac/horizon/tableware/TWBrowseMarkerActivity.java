package uk.ac.horizon.tableware;

import java.net.URI;

import uk.ac.horizon.dtouch.DtouchMarker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class TWBrowseMarkerActivity extends Activity {
	
	private WebView mWebView;
	private DtouchMarker mDtouchMarker;
	private String mUrl;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.browsemarkeresult);
        initDtouchMarker();
        initWebView();
        //initActivityTitle();
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
    }
	
	private void initDtouchMarker(){
		Intent intent = this.getIntent();
		Bundle markerBundle = intent.getExtras();
		if (markerBundle.getString("URLToDisplay") != null)
			mUrl = markerBundle.getString("URLToDisplay");
		mDtouchMarker = DtouchMarker.createMarkerFromBundle(markerBundle);
	}
	
	private void initWebView(){
		if (mDtouchMarker != null && mDtouchMarker.getClass() != null){
			mWebView = (WebView) findViewById(R.id.MarkerwebView);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			
			final Activity activity = this;
			mWebView.setWebChromeClient(new WebChromeClient(){
				public void onProgressChanged(WebView view, int progress){
					activity.setTitle(R.string.web_loading_title);
					activity.setProgress(progress * 1000);
					
					if (progress == 100){
						if (mDtouchMarker != null && mDtouchMarker.getTitle() != null)
							activity.setTitle(mDtouchMarker.getTitle());
						
					}
				}
			});
			
			String encodedURL = appendMemberNameWithURL(mUrl);
			if (encodedURL != null)
				mWebView.loadUrl(encodedURL);
		}
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
