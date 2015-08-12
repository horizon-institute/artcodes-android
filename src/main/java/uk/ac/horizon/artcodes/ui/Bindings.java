package uk.ac.horizon.artcodes.ui;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Bindings
{
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.US);
	private static final SimpleDateFormat dateFormatYear = new SimpleDateFormat("d MMMM yyyy", Locale.US);
	private static ImageLoader imageLoader;

	@BindingAdapter("imageUrl")
	public static void bindImageURL(final ImageView view, String url)
	{
		if (url != null)
		{
			if (url.startsWith("content:") || url.startsWith("file:"))
			{
				view.setImageURI(Uri.parse(url));
			}
			else
			{
				if (imageLoader == null)
				{
					RequestQueue requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
					imageLoader = new ImageLoader(requestQueue,
							new ImageLoader.ImageCache()
							{
								private final LruCache<String, Bitmap>
										cache = new LruCache<>(20);

								@Override
								public Bitmap getBitmap(String url)
								{
									return cache.get(url);
								}

								@Override
								public void putBitmap(String url, Bitmap bitmap)
								{
									cache.put(url, bitmap);
								}
							});
				}

				imageLoader.get(url, new ImageLoader.ImageListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						//if (errorImageResId != 0) {
						//	view.setImageResource(errorImageResId);
						//}
					}

					@Override
					public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate)
					{
						if (response.getBitmap() != null)
						{
							view.setImageBitmap(response.getBitmap());
						}
						//else if (defaultImageResId != 0) {
						//	view.setImageResource(defaultImageResId);
						//}
					}
				});
			}
		}
	}

	@BindingAdapter("textWatcher")
	public static void bindTextWatcher(EditText view, SimpleTextWatcher watcher)
	{
		if (watcher != null)
		{
			view.setText(watcher.getText());
			view.addTextChangedListener(watcher);
		}
	}

	@BindingAdapter("icon")
	public static void bindIcon(Button view, int icon)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
		}
		else
		{
			view.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
		}
	}

	public static String getDate(Long start, Long end)
	{
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());

		Calendar startCal = Calendar.getInstance();
		startCal.setTimeInMillis(start);

		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(end);

		if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR))
		{
			if (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH))
			{
				if (startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH))
				{
					if (endCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
					{
						return dateFormat.format(new Date(end));
					}
					else
					{
						return dateFormatYear.format(new Date(end));
					}
				}
				else if (endCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
				{
					return startCal.get(Calendar.DAY_OF_MONTH) + " – " + dateFormat.format(new Date(end));
				}
				else
				{
					return startCal.get(Calendar.DAY_OF_MONTH) + " – " + dateFormatYear.format(new Date(end));
				}
			}
			if (endCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
			{
				return dateFormat.format(new Date(start)) + " – " + dateFormat.format(new Date(end));
			}
			else
			{
				return dateFormat.format(new Date(start)) + " – " + dateFormatYear.format(new Date(end));
			}
		}
		else
		{
			return dateFormatYear.format(new Date(start)) + " – " + dateFormatYear.format(new Date(end));
		}
	}

	public static String getDate(Long timestamp)
	{
		if (timestamp != null)
		{
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(System.currentTimeMillis());

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp);
			if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR))
			{
				return dateFormat.format(new Date(timestamp));
			}
			else
			{
				return dateFormatYear.format(new Date(timestamp));
			}
		}
		return null;
	}

	@BindingAdapter("date")
	public static void bindDate(TextView view, Long timestamp)
	{
		if (timestamp != null)
		{
			view.setText(getDate(timestamp));
		}
	}
}
