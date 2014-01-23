package uk.ac.horizon.aestheticodes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import uk.ac.horizon.aestheticodes.R;

public class TWWebsiteActivity extends Activity {
	
	private class TWWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}

	
	private WebView mWebView;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.website);
		initWebView();
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
	}
	
	private void initWebView(){
		mWebView = (WebView) findViewById(R.id.markerWebview);
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
					activity.setTitle(R.string.about_web_title);
				}
			}
		});
		
		mWebView.setWebViewClient(new TWWebViewClient());
		Intent intent = getIntent();
		Bundle urlBundle = intent.getExtras();
		mWebView.loadUrl(urlBundle.getString("URL"));
	}
	
}
