package uk.ac.horizon.busabascan;

import java.net.URI;

import uk.ac.horizon.data.DataMarker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class TWBrowseMarkerActivity extends Activity {
	
	private WebView mWebView;
	private DataMarker mDataMarker;
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
		mDataMarker = DataMarker.createMarkerFromBundle(markerBundle);
	}
	
	private void initWebView(){
		if (mDataMarker != null && mDataMarker.getClass() != null){
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
						if (mDataMarker != null && mDataMarker.getTitle() != null)
							activity.setTitle(mDataMarker.getTitle());
						
					}
				}
			});
			
			mWebView.loadUrl(mUrl);
			String encodedURL = appendMemberNameWithURL(mUrl);
			if (encodedURL != null)
				mWebView.loadUrl(encodedURL);
		}
	}
	
	
	private String appendMemberNameWithURL(String url){
		String encodedURL = null;
		TWFacebookUser user = new TWFacebookUser();
		user.restoreMemberFromPreferences(this);
		if (user.name != null){ 
			try{
				url = url.concat("#");
				url = url.concat(user.name.replaceAll(" ", "%20"));
				URI uri = new URI(url);
				encodedURL = uri.toString();
			}
			catch(Exception e){
				encodedURL = null;
			}
		}
		return encodedURL;
	}
	
}
