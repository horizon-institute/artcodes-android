/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.ui;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Bindings
{
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.US);
	private static final SimpleDateFormat dateFormatYear = new SimpleDateFormat("d MMMM yyyy", Locale.US);
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

	@BindingAdapter("imageUrl")
	public static void bindImageURL(final ImageView view, final String url)
	{
		if (url != null)
		{
			if (url.startsWith("content:") || url.startsWith("file:"))
			{
				view.setImageURI(Uri.parse(url));
			}
			else
			{
				Picasso.with(view.getContext())
						.load(url)
						.into(view);
			}
		}
		else
		{
			view.setImageBitmap(null);
		}
	}

	@BindingAdapter("backgroundTintURL")
	public static void bindBackgroundTintURL(final View view, String url)
	{
		if (url != null)
		{
			Picasso.with(view.getContext()).load(url).into(new Target()
			{
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						Palette.from(bitmap).generate(new Palette.PaletteAsyncListener()
						{
							@TargetApi(Build.VERSION_CODES.LOLLIPOP)
							public void onGenerated(Palette p)
							{
								int color = p.getDarkMutedColor(Color.BLACK);
								if (color != Color.BLACK)
								{
									view.setBackgroundTintList(ColorStateList.valueOf(color));
								}
							}
						});
					}
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable)
				{
				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable)
				{

				}
			});
		}
	}

	@BindingAdapter("decoration")
	public static void setDecoration(RecyclerView view, RecyclerView.ItemDecoration itemDecoration)
	{
		view.invalidateItemDecorations();
		if (itemDecoration != null)
		{
			view.addItemDecoration(itemDecoration);
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

	public static String getTime(Long timestamp)
	{
		if (timestamp != null)
		{
			long now = System.currentTimeMillis();
			Date nowDate = new Date(now);
			Date date = new Date(timestamp);

			String today = dateFormat.format(nowDate);
			String dateString = dateFormat.format(date);
			if (!today.equals(dateString))
			{
				return timeFormat.format(date) + " " + getDate(timestamp);
			}

			long difference = now - timestamp;
			if (difference < 120000)
			{
				return "Just Now";
			}
			else if (difference < 3600000)
			{
				int minutes = (int) (difference / 60000);
				return minutes + " mins ago";
			}
			else
			{
				return timeFormat.format(date);
			}
		}
		return null;
	}

	@BindingAdapter("time")
	public static void bindTime(TextView view, Long timestamp)
	{
		if (timestamp != null)
		{
			view.setText(getTime(timestamp));
		}
		else
		{
			view.setText(null);
		}
	}


	@BindingAdapter("date")
	public static void bindDate(TextView view, Long timestamp)
	{
		if (timestamp != null)
		{
			view.setText(getDate(timestamp));
		}
		else
		{
			view.setText(null);
		}
	}
}
