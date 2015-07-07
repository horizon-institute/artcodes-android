package uk.ac.horizon.artcodes.model.loader;

import android.content.Context;

public class Ref<T>
{
	private final String uri;
	private T item;

	public Ref(String uri)
	{
		this.uri = uri;
	}

	public T get()
	{
		return item;
	}

	public String getUri()
	{
		return uri;
	}

	public boolean isLoaded()
	{
		return item != null;
	}

	public void load(Context context, LoadListener<T> listener)
	{
		if (isLoaded())
		{
			listener.onLoaded(item);
		}
	}

	protected void set(T item)
	{
		this.item = item;
	}
}
