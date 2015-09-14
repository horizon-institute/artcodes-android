package uk.ac.horizon.aestheticodes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import uk.ac.horizon.aestheticodes.R;

public class WebActivity extends ActionBarActivity
{
    private WebViewClient webViewClient = null;
    private String caller = null;
    private Context context;

    private void saveSelectedExperienceToPreferences(String experienceId)
    {
        if(experienceId != null)
        {
            Log.i("EXPERIENCE_PREF", "Setting experience id " + experienceId + "(from web link)");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("experience", experienceId);
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        this.context = this;

        String url = getIntent().getStringExtra("URL");
        this.caller = getIntent().getStringExtra("caller");

        this.webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                String goToExperienceString = "?go_to_experience=";
                if (url.contains("?go_to_camera"))
                {
                    Intent intent = new Intent();
                    intent.putExtra("caller", this.getClass().getCanonicalName());
                    intent.setClass(context, AestheticodesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if (url.contains("?go_to_start"))
                {
                    Intent intent = new Intent();
                    intent.putExtra("caller", this.getClass().getCanonicalName());
                    intent.setClass(context, SelectorActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if (url.contains(goToExperienceString))
                {
                    String experienceId = url.substring(url.indexOf(goToExperienceString)+goToExperienceString.length());
                    if (experienceId!=null)
                    {
                        saveSelectedExperienceToPreferences(experienceId);
                    }
                    return true;
                }
                else
                {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        };

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(this.webViewClient);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setInitialScale(getScale());
        if (url!=null)
        {
            webView.loadUrl(url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int getScale(){
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        double scale = (((double)width)/480d)*100d;
        return (int)scale;
    }
}
