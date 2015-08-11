package uk.ac.horizon.artcodes.request;

import android.util.Log;
import uk.ac.horizon.artcodes.GoogleAnalytics;

public abstract class RequestCallbackBase<T> implements RequestCallback<T>
{
	@Override
	public void onError(Exception e)
	{
		GoogleAnalytics.trackException(e);
	}
}
