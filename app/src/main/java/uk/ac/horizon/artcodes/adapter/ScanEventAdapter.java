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

package uk.ac.horizon.artcodes.adapter;

import android.app.Activity;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ScanEventBinding;
import uk.ac.horizon.artcodes.model.ScanEvent;

public class ScanEventAdapter extends ListAdapter<ScanEventBinding>
{
	private final SortedList<ScanEvent> history;
	private final Activity activity;

	public ScanEventAdapter(final Activity activity)
	{
		super(activity);
		this.activity = activity;
		history = new SortedList<>(ScanEvent.class, new SortedListAdapterCallback<ScanEvent>(adapter)
		{
			@Override
			public boolean areContentsTheSame(ScanEvent oldItem, ScanEvent newItem)
			{
				return oldItem.getTimestamp() == newItem.getTimestamp();
			}

			@Override
			public boolean areItemsTheSame(ScanEvent item1, ScanEvent item2)
			{
				return item1.getTimestamp() == item2.getTimestamp();
			}

			@Override
			public int compare(ScanEvent o1, ScanEvent o2)
			{
				return (int) (o2.getTimestamp() - o1.getTimestamp());
			}
		});
	}

	public void add(ScanEvent item)
	{
		history.add(item);
	}

	public void setHistory(List<ScanEvent> history)
	{
		this.history.clear();
		this.history.addAll(history);
		adapter.notifyDataSetChanged();
	}

	@Override
	public ScanEventBinding createBinding(final ViewGroup parent, final int viewType)
	{
		return ScanEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
	}

	@Override
	public void bind(final int position, final ScanEventBinding binding)
	{
		final ScanEvent scanEvent = history.get(position);
		binding.setScanEvent(scanEvent);
		binding.getRoot().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
				//builder.setSession(session);
				builder.setToolbarColor(ContextCompat.getColor(context, R.color.apptheme_primary));
				CustomTabsIntent customTabsIntent = builder.build();
				customTabsIntent.launchUrl(activity, Uri.parse(scanEvent.getAction().getUrl()));
			}
		});
	}

	@Override
	public int getViewCount()
	{
		return history.size();
	}
}
