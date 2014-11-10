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

package uk.ac.horizon.aestheticodes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;

public class ExperienceAdapter extends BaseAdapter
{
	private final Context context;
	private final LayoutInflater inflater;
	private final ExperienceManager experienceManager;

	public ExperienceAdapter(final Context context, final ExperienceManager experienceManager)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.experienceManager = experienceManager;
	}

	@Override
	public int getCount()
	{
		return experienceManager.list().size();
	}

	@Override
	public Object getItem(int i)
	{
		return experienceManager.list().get(i);
	}

	@Override
	public long getItemId(final int position)
	{
		return experienceManager.list().get(position).getId().hashCode();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		final Experience experience = experienceManager.list().get(i);
		if (view == null)
		{
			view = inflater.inflate(R.layout.experience_listitem, viewGroup, false);
		}

		final LinearLayout layout = (LinearLayout) view.findViewById(R.id.rootView);
		if(viewGroup.getClass().getName().endsWith("SpinnerCompat"))
		{

			layout.setPadding(0,0,8,0);
		}

		final TextView eventTitle = (TextView) view.findViewById(R.id.markerCode);
		final ImageView iconView = (ImageView) view.findViewById(R.id.experienceIcon);
		eventTitle.setText(experience.getName());

		iconView.setSelected(false);
		if (experience.getIcon() == null)
		{
			iconView.setVisibility(View.GONE);
		}
		else
		{
			iconView.setVisibility(View.VISIBLE);
			Picasso.with(context).cancelRequest(iconView);
			Picasso.with(context).load(experience.getIcon()).placeholder(R.drawable.ic_action_labels_light).into(iconView);
		}

		return view;
	}
}
