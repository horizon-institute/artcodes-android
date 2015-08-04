package uk.ac.horizon.artcodes.source;

public interface UriList<T> extends Source<T>
{
	void add(String uri);

	boolean contains(String uri);

	void remove(String uri);
}
