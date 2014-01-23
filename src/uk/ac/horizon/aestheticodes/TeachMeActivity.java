package uk.ac.horizon.aestheticodes;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class TeachMeActivity extends Activity
{
	TextView aboutTextView;
	WebView mWebview;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// super.onCreate(savedInstanceState);
		// setContentView(R.layout.aboutview);
		//
		// aboutTextView = (TextView) findViewById(R.id.aboutTextView);
		// aboutTextView.setText("About Aestheticodes");
		//
		// aboutWebView = (WebView) findViewById(R.id.aboutWebView);
		//
		// // Load the URL from the strings resource: easier to change in one
		// place
		// // at later stage
		// aboutWebView.loadUrl(getResources().getString(R.string.about_aestheticodes));
		super.onCreate(savedInstanceState);

		mWebview = new WebView(this);

		// mWebview.getSettings().setJavaScriptEnabled(true); // enable
		// javascript

		final Activity activity = this;

		mWebview.setWebViewClient(new WebViewClient()
		{
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
			}
		});

		activity.setTitle("Teach Me");
		mWebview.loadUrl(getResources().getString(R.string.teachme_aestheticodes));
		setContentView(mWebview);

	}
}
