package uk.ac.horizon.artcodes.source;

/**
 * Created by kevin on 02/08/2015.
 */
public interface Source<T>
{
	void loadInto(Target<T> target);
}
