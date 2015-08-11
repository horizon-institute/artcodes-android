package uk.ac.horizon.artcodes.request;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class HTTPRequest<T> extends UriSource<T>
{
	public static final class Factory implements RequestFactory
	{

		@Override
		public <T> Request<T> createRequest(ArtcodeServer server, String uri, Type type)
		{
			return new HTTPRequest<>(server, uri, type);
		}

		@Override
		public String[] getPrefixes()
		{
			return new String[]{"http:", "https:"};
		}
	}
	private static final int timeout = 10000;
	private static RequestQueue requestQueue;

	public static RequestQueue getQueue(Context context)
	{
		if (requestQueue == null)
		{
			requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		}
		return requestQueue;
	}

	public HTTPRequest(ArtcodeServer server, String uri, Type type)
	{
		super(server, uri, type);
	}

	@Override
	public void loadInto(final RequestCallback<T> target)
	{
		com.android.volley.Request request = new StringRequest(com.android.volley.Request.Method.GET, uri, new Response.Listener<String>()
		{
			@Override
			public void onResponse(String response)
			{
				try
				{
					target.onResponse(server.getGson().<T>fromJson(response, type));
				}
				catch (Exception e)
				{
					target.onError(e);
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				onError(target, error);
			}
		})
		{
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError
			{
				return getRequestHeaders();
			}
		};

		RequestQueue requestQueue = getQueue(server.getContext());
		request.setRetryPolicy(new DefaultRetryPolicy(
				timeout,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(request);
	}

	protected void onError(RequestCallback<T> target, VolleyError error)
	{
		target.onError(error);
	}

	protected Map<String, String> getRequestHeaders()
	{
		return Collections.emptyMap();
	}
}
