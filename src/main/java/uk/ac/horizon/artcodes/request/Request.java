package uk.ac.horizon.artcodes.request;


public interface Request<T>
{
	void loadInto(RequestCallback<T> target);
}
