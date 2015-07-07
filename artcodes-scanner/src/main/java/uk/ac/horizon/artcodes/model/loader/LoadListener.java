package uk.ac.horizon.artcodes.model.loader;

import android.util.Log;

public abstract class LoadListener<T>
{
	public void onLoadError(Exception e)
	{
		Log.e("", "Error loading " + e.getMessage(), e);
	}

	public abstract void onLoaded(T item);
}
