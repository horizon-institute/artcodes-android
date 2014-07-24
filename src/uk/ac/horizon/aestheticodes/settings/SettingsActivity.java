/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import uk.ac.horizon.aestheticodes.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends FragmentActivity
{
	public static class Adapter extends BaseAdapter
	{
		private final List<SettingsItem> items = new ArrayList<SettingsItem>();
		private final LayoutInflater inflater;

		public Adapter(final Context context)
		{
			inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		public void add(SettingsItem item)
		{
			items.add(item);
		}

		public void clear()
		{
			items.clear();
		}

		@Override
		public int getViewTypeCount()
		{
			return SettingsItem.Type.values().length;
		}

		@Override
		public int getItemViewType(int position)
		{
			final SettingsItem item = items.get(position);
			return item.getType().ordinal();
		}

		@Override
		public int getCount()
		{
			return items.size();
		}

		@Override
		public Object getItem(int i)
		{
			return items.get(i);
		}

		@Override
		public long getItemId(final int position)
		{
			return items.get(position).hashCode();
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{
			final SettingsItem item = items.get(i);
			if (item.getType() == SettingsItem.Type.header)
			{
				if (view == null)
				{
					view = inflater.inflate(R.layout.item_settings_header, viewGroup, false);
				}
			}
			else if (item.getType() == SettingsItem.Type.single_line)
			{
				if (view == null)
				{
					view = inflater.inflate(R.layout.item_settings_item, viewGroup, false);
				}

				final ImageView iconView = (ImageView) view.findViewById(R.id.settings_item_icon);
				if (item.getIcon() == 0)
				{
					iconView.setVisibility(View.GONE);
				}
				else
				{
					iconView.setVisibility(View.VISIBLE);
					iconView.setImageResource(item.getIcon());
				}
			}
			else if (item.getType() == SettingsItem.Type.two_line || item.getType() == SettingsItem.Type.two_line_disabled)
			{
				if (view == null)
				{
					if (item.getType() == SettingsItem.Type.two_line)
					{
						view = inflater.inflate(R.layout.item_settings_value, viewGroup, false);
					}
					else
					{
						view = inflater.inflate(R.layout.item_settings_value_disabled, viewGroup, false);
					}
				}

				final TextView eventTitle = (TextView) view.findViewById(R.id.settings_item_detail);
				eventTitle.setText(item.getDetail());

				final ImageView iconView = (ImageView) view.findViewById(R.id.settings_item_icon);
				if (item.getIcon() == 0)
				{
					iconView.setVisibility(View.GONE);
				}
				else
				{
					iconView.setVisibility(View.VISIBLE);
					iconView.setImageResource(item.getIcon());
				}
			}

			final TextView eventTitle = (TextView) view.findViewById(R.id.settings_item_title);
			eventTitle.setText(item.getTitle());

			return view;
		}

	}

	protected Adapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		ListView listView = (ListView) findViewById(R.id.listView);
		adapter = new Adapter(this);
		listView.setAdapter(adapter);

		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.setBackgroundColor(getResources().getColor(R.color.list_background));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id)
			{
				SettingsItem item = (SettingsItem) adapter.getItem(position);
				item.selected();
			}
		});

		getActionBar().setDisplayHomeAsUpEnabled(true);

		refresh();
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void refresh()
	{
		adapter.notifyDataSetChanged();
	}

	public void setProperty(String propertyName, Object value)
	{
	}
}
