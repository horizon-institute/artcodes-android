package uk.ac.horizon.artcodes.storage;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Collections;
import java.util.Map;

public class HTTPStore implements Store
{
	private static final int timeout = 10000;
	protected final Context context;

	public HTTPStore(Context context)
	{
		this.context = context;
	}

	@Override
	public boolean canLoad(String uri)
	{
		return uri.startsWith("http://") || uri.startsWith("https://");
	}

	@Override
	public void load(final Loader<?> loader)
	{
		Request<?> request = new StringRequest(com.android.volley.Request.Method.GET, loader.getUri(), new Response.Listener<String>()
		{
			@Override
			public void onResponse(String response)
			{
				loader.parse(response);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{

			}
		})
		{
			@Override
			public void addMarker(String tag)
			{
				super.addMarker(tag);
				Log.i("", loader.getUri() + ": " + tag);
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError
			{
				return getRequestHeaders();
			}
		};
		RequestQueue requestQueue = getQueue();
//		Cache.Entry entry = requestQueue.getCache().get(request.getCacheKey());
//		if (entry != null)
//		{
//			try
//			{
//				loader.parse(new String(entry.data, HttpHeaderParser.parseCharset(entry.responseHeaders)));
//			}
//			catch (Exception e)
//			{
//				Log.w("", e.getMessage(), e);
//			}
//		}

		request.setRetryPolicy(new DefaultRetryPolicy(
				timeout,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(request);
	}

	protected RequestQueue getQueue()
	{
		return Volley.newRequestQueue(context.getApplicationContext());
	}

	protected Map<String, String> getRequestHeaders()
	{
		return Collections.emptyMap();
	}
}
