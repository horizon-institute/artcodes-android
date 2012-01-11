package uk.ac.horizon.tableware;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class TWBrowseMarkerActivity extends Activity {
	
	private WebView mWebView;
	private static final String url = "http://busaba.com/#news=false&recipes=true";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browsemarkeresult);
        initWebView();
    }
	
	private void initWebView(){
		mWebView = (WebView) findViewById(R.id.MarkerwebView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(url);
	}
}
