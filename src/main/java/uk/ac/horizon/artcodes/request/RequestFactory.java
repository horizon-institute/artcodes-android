package uk.ac.horizon.artcodes.request;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.lang.reflect.Type;

public interface RequestFactory
{
	String[] getPrefixes();

	<T> Request<T> createRequest(ArtcodeServer server, String uri, Type type);
}
