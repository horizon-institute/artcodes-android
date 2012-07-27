package uk.ac.horizon.busabascan;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class TWNewsActivity extends Activity {
	private WebView mWebView;
	private static final String NEWS_URL = "http://busaba.com/#";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.news);
		initWebView();
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
	}
	
	private void initWebView(){
		mWebView = (WebView) findViewById(R.id.newsWebview);
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
					activity.setTitle(R.string.news_web_title);
				}
			}
		});
		mWebView.loadUrl(NEWS_URL);
	}
}
