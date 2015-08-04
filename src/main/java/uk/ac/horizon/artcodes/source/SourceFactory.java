package uk.ac.horizon.artcodes.source;

import uk.ac.horizon.artcodes.account.Account;

import java.lang.reflect.Type;

public interface SourceFactory
{
	String[] getPrefixes();

	<T> Source<T> createSource(Account account, String uri, Type type);
}
