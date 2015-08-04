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

	@BindingAdapter("date")
	public static void bindDate(TextView view, Long timestamp)
	{
		if (timestamp != null)
		{
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(System.currentTimeMillis());


			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp);
			if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR))
			{
				view.setText(dateFormat.format(new Date(timestamp)));
			}
			else
			{
				view.setText(dateFormatYear.format(new Date(timestamp)));
			}
		}
	}
}
