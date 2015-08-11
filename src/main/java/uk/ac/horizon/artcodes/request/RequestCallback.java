package uk.ac.horizon.artcodes.request;

public interface RequestCallback<T>
{
	void onResponse(T item);
	
	void onError(Exception e);
}
