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

package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

class RequestBodyUtil
{
	public static class ImageRequestBody extends RequestBody
	{
		private final Context context;
		private final Uri uri;
		private int maxSize = 500;
		private Bitmap bitmap;

		private ImageRequestBody(Context context, Uri uri)
		{
			this.context = context;
			this.uri = uri;
		}

		public String getHash() throws IOException
		{
			bitmap = resizedBitmap();
			if (bitmap == null)
			{
				HashingInputStream inputStream = new HashingInputStream(Hashing.sha256(), getInputStream());
				ByteStreams.copy(inputStream, ByteStreams.nullOutputStream());
				inputStream.close();
				return inputStream.hash().toString();
			}
			else
			{
				HashingOutputStream outputStream = new HashingOutputStream(Hashing.sha256(), ByteStreams.nullOutputStream());
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
				outputStream.close();
				return outputStream.hash().toString();
			}
		}

		@Override
		public MediaType contentType()
		{
			if (bitmap == null)
			{
				return MediaType.parse("image/jpeg");
			}
			else
			{
				return MediaType.parse(context.getContentResolver().getType(uri));
			}
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException
		{
			if (bitmap != null)
			{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, sink.outputStream());
			}
			else
			{
				Source source = null;
				try
				{
					source = Okio.source(getInputStream());
					sink.writeAll(source);
				}
				finally
				{
					Util.closeQuietly(source);
				}
			}
		}

		private InputStream getInputStream() throws IOException
		{
			return context.getContentResolver().openInputStream(uri);
		}

		private Bitmap resizedBitmap() throws IOException
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(getInputStream(), null, o);

			int width_tmp = o.outWidth;
			int height_tmp = o.outHeight;
			int scale = 1;

			while (width_tmp > maxSize && height_tmp > maxSize)
			{
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			if (scale != 1)
			{
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				return BitmapFactory.decodeStream(getInputStream(), null, o2);
			}
			return null;
		}
	}

	public static ImageRequestBody createImageBody(Context context, String uri)
	{
		return new ImageRequestBody(context, Uri.parse(uri));
	}

	public static RequestBody create(final MediaType mediaType, final InputStream inputStream)
	{
		return new RequestBody()
		{
			@Override
			public MediaType contentType()
			{
				return mediaType;
			}

			@Override
			public long contentLength()
			{
				try
				{
					return inputStream.available();
				}
				catch (IOException e)
				{
					return 0;
				}
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException
			{
				Source source = null;
				try
				{
					source = Okio.source(inputStream);
					sink.writeAll(source);
				}
				finally
				{
					Util.closeQuietly(source);
				}
			}
		};
	}
}