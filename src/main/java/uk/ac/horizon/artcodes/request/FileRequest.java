/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.request;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URI;

import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class FileRequest<T> extends UriSource<T>
{
	public FileRequest(ArtcodeServer server, String uri, Type type)
	{
		super(server, uri, type);
	}

	@Override
	public void loadInto(RequestCallback<T> callback)
	{
		try
		{
			callback.onResponse(server.getGson().<T>fromJson(new FileReader(new File(URI.create(uri))), type));
		} catch (Exception e)
		{
			callback.onError(e);
		}
	}

	public static final class Factory implements RequestFactory
	{
		@Override
		public String[] getPrefixes()
		{
			return new String[]{"file:"};
		}

		@Override
		public <T> Request<T> createRequest(ArtcodeServer server, String uri, Type type)
		{
			return new FileRequest<>(server, uri, type);
		}
	}
}
