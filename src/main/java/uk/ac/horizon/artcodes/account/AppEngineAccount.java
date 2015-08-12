package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.HTTPRequest;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppEngineAccount implements Account
{
	private final ArtcodeServer server;
	private final String name;
	//private static final Map<String, AppEngineUpload> requests = new HashMap<>();

	public AppEngineAccount(ArtcodeServer server, String name)
	{
		this.server = server;
		this.name = name;
	}

	public String getToken()
	{
		try
		{
			return GoogleAuthUtil.getToken(server.getContext(), name, "oauth2:email");
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
		return null;
	}

	@Override
	public void loadLibrary(final RequestCallback<List<String>> callback)
	{
		server.load("https://aestheticodes.appspot.com/experiences", new TypeToken<List<String>>() {}.getType(), new RequestCallbackBase<List<String>>()
		{
			@Override
			public void onResponse(List<String> item)
			{
				SharedPreferences.Editor editor = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
				for (String uri : item)
				{
					editor.putString(uri, getId());
				}
				editor.apply();
				callback.onResponse(item);
			}
		});
	}

	@Override
	public void saveExperience(final Experience experience)
	{
		new Thread(new Runnable()
		{
			private static final String rootHTTP = "http://aestheticodes.appspot.com/experience";
			private static final String rootHTTPS = "https://aestheticodes.appspot.com/experience";
			private static final int imageMaxSize = 1024;

			@Override
			public void run()
			{
				try
				{
					// TODO Save temp file, notify starting

					// If auto upload
					experience.update();

					final Set<String> images = new HashSet<>();
					conditionalAdd(images, experience.getImage());
					conditionalAdd(images, experience.getIcon());

					for (String imageURI : images)
					{
						final Uri uri = Uri.parse(imageURI);
						final Bitmap bitmap = resizedBitmap(uri);

						final String hash = getHash(uri, bitmap);
						final String url = "https://aestheticodes.appspot.com/image/" + hash;

						boolean success = exists(url);
						if (!success)
						{
							HttpURLConnection connection = save(imageURI, bitmap, url);
							success = connection.getResponseCode() == 200;
						}

						if (success)
						{
							if (imageURI.equals(experience.getImage()))
							{
								experience.setImage(url);
							}

							if (imageURI.equals(experience.getIcon()))
							{
								experience.setIcon(url);
							}
							Log.i("", imageURI + " is now " + url);
						}
					}

					HttpURLConnection connection = save(experience);
					byte[] bytes = ByteStreams.toByteArray(connection.getInputStream());
					Map<String, String> headers = new HashMap<>();
					for (String headerName : connection.getHeaderFields().keySet())
					{
						headers.put(headerName, connection.getHeaderField(headerName));
					}

					String charset = HttpHeaderParser.parseCharset(headers, "UTF-8");
					Experience saved = server.getGson().fromJson(new InputStreamReader(new ByteArrayInputStream(bytes), charset), Experience.class);

					SharedPreferences.Editor editor = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
					editor.putString(saved.getId(), getId()).apply();

					HTTPRequest.getQueue(server.getContext()).getCache().remove(saved.getId());
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}

			private void conditionalAdd(Set<String> urls, String url)
			{
				if (url != null && (url.startsWith("file:") || url.startsWith("content:")))
				{
					urls.add(url);
				}
			}

			private boolean exists(String url) throws Exception
			{
				HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
				connection.setRequestMethod("HEAD");
				connection.connect();
				return connection.getResponseCode() == 200;
			}

			private String getHash(Uri uri, Bitmap bitmap) throws IOException
			{
				if (bitmap == null)
				{
					HashingInputStream inputStream = new HashingInputStream(Hashing.sha256(), getInputStream(uri));
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

			private InputStream getInputStream(Uri uri) throws IOException
			{
				if (uri.getScheme().equals("content"))
				{
					return server.getContext().getContentResolver().openInputStream(uri);
				}
				else if (uri.getScheme().equals("file"))
				{
					return new FileInputStream(new File(uri.getPath()));
				}
				throw new IOException("Could not open " + uri.toString());
			}

			private Bitmap resizedBitmap(Uri uri) throws IOException
			{
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(getInputStream(uri), null, o);

				int width_tmp = o.outWidth;
				int height_tmp = o.outHeight;
				int scale = 1;

				while (width_tmp > imageMaxSize && height_tmp > imageMaxSize)
				{
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}

				if (scale != 1)
				{
					BitmapFactory.Options o2 = new BitmapFactory.Options();
					o2.inSampleSize = scale;
					return BitmapFactory.decodeStream(getInputStream(uri), null, o2);
				}
				return null;
			}

			private HttpURLConnection save(String imageURI, Bitmap bitmap, String url) throws Exception
			{
				HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());

				connection.setDoOutput(true);
				connection.setRequestMethod("PUT");
				connection.setRequestProperty("Content-Type", "application/json");
				String token = null;
				try
				{
					token = getToken();
					connection.setRequestProperty("Authorization", "Bearer " + token);
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}

				if (bitmap == null)
				{
					InputStream inputStream = getInputStream(Uri.parse(imageURI));
					ByteStreams.copy(inputStream, connection.getOutputStream());
				}
				else
				{
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, connection.getOutputStream());
				}

				connection.connect();
				if (connection.getResponseCode() == 401)
				{
					Log.w("", "Response " + connection.getResponseCode());
					if (token != null)
					{
						GoogleAuthUtil.invalidateToken(server.getContext(), token);
					}
				}
				else if (connection.getResponseCode() != 200)
				{
					Log.w("", "Response " + connection.getResponseCode() + ": " + connection.getResponseMessage());
				}

				return connection;
			}

			private HttpURLConnection save(Experience experience) throws Exception
			{
				String url = experience.getId();
				String method = "PUT";

				Log.i("", "Saving id = " + url);

				if(url == null)
				{
					url = rootHTTPS;
					method = "POST";
				}
				else
				{
					url = url.toLowerCase();
					if(url.startsWith(rootHTTP))
					{
						Log.i("", "== http:// :" + url);
						url = url.replace("http://", "https://");
					}
					else if(!url.startsWith(rootHTTPS))
					{
						Log.i("", "!= https:// :" + url);
						url = rootHTTPS;
						method = "POST";
					}
				}

				HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());

				connection.setDoOutput(true);
				connection.setRequestMethod(method);
				connection.setRequestProperty("Content-Type", "application/json");
				String token = null;
				try
				{
					token = getToken();
					connection.setRequestProperty("Authorization", "Bearer " + token);
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}

				String experienceJSON = server.getGson().toJson(experience);
				Log.i("", "Saving " + experienceJSON);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(experienceJSON);
				out.close();

				connection.connect();
				if (connection.getResponseCode() == 401)
				{
					Log.w("", "Response " + connection.getResponseCode());
					if (token != null)
					{
						GoogleAuthUtil.invalidateToken(server.getContext(), token);
					}
				}
				else if (connection.getResponseCode() != 200)
				{
					Log.w("", "Response " + connection.getResponseCode() + ": " + connection.getResponseMessage());
				}

				return connection;
			}
		}).start();
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Account && ((Account) o).getId().equals(getId());
	}

	@Override
	public String getId()
	{
		return "google:" + name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public String getName()
	{
		return name;
		//return server.getContext().getString(R.string.server, name);
	}
}
