/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.aestheticodes.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import uk.ac.horizon.aestheticodes.R;

public class AboutActivity extends Activity
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

		activity.setTitle("About Aestheticodes");
		mWebview.loadUrl(getResources().getString(R.string.about_aestheticodes));
		setContentView(mWebview);

	}
}
