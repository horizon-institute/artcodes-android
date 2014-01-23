package uk.ac.horizon.aestheticodes;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import uk.ac.horizon.aestheticodes.MarkerPopupWindow.OnMarkerPopupWindowListener;
import uk.ac.horizon.aestheticodes.MarkerPopupWindowWithImage.OnMarkerPopupWindowWithImageListener;
import uk.ac.horizon.aestheticodes.TWMarkerSurfaceView.OnMarkerDetectedListener;
import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
import uk.ac.horizon.dtouchMobile.DtouchMarker;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class TWCameraActivity extends Activity implements OnMarkerDetectedListener, OnMarkerPopupWindowListener, OnMarkerPopupWindowWithImageListener
{
	public static final int VIEW_MODE_MARKER = 0;
	public static final int VIEW_MODE_MARKER_DEBUG = 1;
	static final int DIALOG_MARKER_DETECTION_ID = 0;
	private static final String TAG = "Tableware::TablewareActivity";

	private MenuItem mItemDetectMarker;
	private MenuItem mItemDetectMarkerDebug;
	// private MenuItem mItemPreference;

	private TWMarkerSurfaceView mMarkerSurfaceView;

	public static int viewMode = VIEW_MODE_MARKER;

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this)
	{
		@Override
		public void onManagerConnected(int status)
		{
			switch (status)
			{
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				// Create and set View
				// setContentView(R.layout.main);
				setContentView(R.layout.markercamera);
				initTWSurfaceViewListener();
				startMarkerDetectionProcess();
			}
				break;
			default:
			{
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// setContentView(R.layout.markercamera);
		// initTWSurfaceViewListener();

		// mWebView.loadUrl(urlBundle.getString("URL"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		mItemDetectMarker = menu.add(R.string.detect_marker);
		mItemDetectMarkerDebug = menu.add(R.string.detect_marker_debug);
		// mItemPreference = menu.add(R.string.view_preferences);

		return true;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "On Resume");
		// startMarkerDetectionProcess();
		Log.i(TAG, "Trying to load OpenCV library");
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this.getApplicationContext(), mOpenCVCallBack))
		{
			Log.e(TAG, "Cannot connect to OpenCV Manager");
		}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Log.d(TAG, "On Resume");
		stopMarkerDetectionProcess();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == mItemDetectMarker)
			viewMode = VIEW_MODE_MARKER;
		else if (item == mItemDetectMarkerDebug)
			viewMode = VIEW_MODE_MARKER_DEBUG;
		// else if (item == mItemPreference)
		// displayPreferences();

		return true;
	}

	private void displayPreferences()
	{
		Intent intent = new Intent(this, TWPreferenceActivity.class);
		startActivity(intent);
	}

	private void stopMarkerDetectionProcess()
	{

		// Check the view exists
		if (mMarkerSurfaceView != null)
			mMarkerSurfaceView.stopProcessing();

	}

	private void startMarkerDetectionProcess()
	{
		mMarkerSurfaceView.startProcessing();
	}

	public void onMarkerDetected(final DtouchMarker marker)
	{
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				// showProgressControls();
				getMarker(marker.getCodeKey());
			}
		});
	}

	void getMarker(String code)
	{
		DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener()
		{
			public void onMarkerDownloaded(DataMarker marker)
			{
				markerDownloaded(marker);
			}

			@Override
			public void onMarkerDownloadError()
			{
				markerDownloadWithError();
			}
		});
		// dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, null);
		dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, null, this);
	}

	public void displayMarkerDetail(DataMarker marker)
	{
		displayMarker(marker);
	}

	private void initTWSurfaceViewListener()
	{
		mMarkerSurfaceView = (TWMarkerSurfaceView) findViewById(R.id.MarkerSurfaceView);
		mMarkerSurfaceView.setOnMarkerDetectedListener(this);
		mMarkerSurfaceView.setPreference(new HIPreferenceTableware(this));
	}

	private void markerDownloaded(DataMarker marker)
	{
		// hideProgressControls();
		if (marker != null)
		{
			stopMarkerDetectionProcess();
			mMarkerSurfaceView.stopDisplayingDetectedMarker();
			displayMarkerDetail(marker);
		}
		else
			mMarkerSurfaceView.stopDisplayingDetectedMarker();
	}

	private void markerDownloadWithError()
	{
		// hideProgressControls();
		MessageDialog.showMessage(R.string.downloadErrorMsg, this);
	}

	// OnMarkerPopupWindowWithImageListener methods.
	public void onImageDismissedSelected(DataMarker marker)
	{
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
	}

	public void onImageBrowseMarkerSelected(DataMarker marker)
	{
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
	}

	// OnMarkerPopupWindowListener methods.
	public void onDismissedSelected(DataMarker marker)
	{
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
	}

	public void onBrowseMarkerSelected(DataMarker marker)
	{
		mMarkerSurfaceView.stopDisplayingDetectedMarker();
		stopMarkerDetectionProcess();
		displayMarkerDetail(marker);
	}

	// services function.
	private void displayMarker(DataMarker marker)
	{
		switch (marker.getServiceId())
		{
		case DataMarker.YOU_TUBE:
			displayYouTube();
			break;
		case DataMarker.MAIL:
			displayMail();
			break;
		case DataMarker.WEBSITE:
			displayWebsite(marker);
		default:
			break;
		}
	}

	private void displayYouTube()
	{
		String VIEDO_ID = "cKd8NXWwvKI";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + VIEDO_ID));
		startActivity(intent);
	}

	private void displayWebsite(DataMarker marker)
	{
		// Open the webpage in the device's browser, not in the app:
		Bundle urlBundle = createURLBundleFromMaker(marker);
		String url = urlBundle.getString("URL");
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);

		// Open the webpage in side of the application:
		// Bundle urlBundle = createURLBundleFromMaker(marker);
		// Intent intent = new Intent(this, TWWebsiteActivity.class);
		// intent.putExtras(urlBundle);
		// startActivity(intent);
	}

	private Bundle createURLBundleFromMaker(DataMarker marker)
	{
		Bundle urlBundle = new Bundle();
		urlBundle.putString("URL", marker.getUri());
		return urlBundle;
	}

	private void displayMail()
	{
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		/* Fill it with Data */
		intent.setType("plain/text");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "abc@email.com" });
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
		/* Send it off to the Activity-Chooser */
		this.startActivity(Intent.createChooser(intent, "Send mail..."));
	}

}