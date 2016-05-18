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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.ExperienceCardBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.server.LoadCallback;

public abstract class ExperienceAdapter extends GridAdapter<ExperienceCardBinding> implements LoadCallback<List<String>>
{
	final ArtcodeServer server;

	ExperienceAdapter(Context context, final ArtcodeServer server)
	{
		super(context);
		this.server = server;
	}

	@Override
	public void bind(final int position, final ExperienceCardBinding binding)
	{
		final Experience experience = getExperience(position);
		binding.setExperience(experience);
		binding.getRoot().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ExperienceActivity.start(context, experience);
			}
		});
		binding.scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ArtcodeActivity.start(context, experience);
			}
		});
	}
	
	protected abstract Experience getExperience(int position);

	@Override
	public ExperienceCardBinding createBinding(final ViewGroup parent, final int viewType)
	{
		return ExperienceCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
	}

	@Override
	public void error(Throwable e)
	{
		loadFinished();
		showError(context.getString(R.string.connection_error));
	}
}
