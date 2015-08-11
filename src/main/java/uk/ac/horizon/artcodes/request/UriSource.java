package uk.ac.horizon.artcodes.request;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.lang.reflect.Type;

public abstract class UriSource<T> implements Request<T>
{
	protected final ArtcodeServer server;
	protected final String uri;
	protected final Type type;

	protected UriSource(ArtcodeServer server, String uri, Type type)
	{
		this.uri = uri;
		this.server = server;
		this.type = type;
	}
}
