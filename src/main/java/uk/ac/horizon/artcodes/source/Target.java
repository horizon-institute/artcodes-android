package uk.ac.horizon.artcodes.source;

public interface Target<T>
{
	void onLoaded(T item);
}
