package uk.ac.horizon.artcodes.source;

import uk.ac.horizon.artcodes.account.Account;

import java.lang.reflect.Type;

public abstract class UriSource<T> implements Source<T>
{
	protected final Account account;
	protected final String uri;
	protected final Type type;

	protected UriSource(Account account, String uri, Type type)
	{
		this.uri = uri;
		this.account = account;
		this.type = type;
	}
}
