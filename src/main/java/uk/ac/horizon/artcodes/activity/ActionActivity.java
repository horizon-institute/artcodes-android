/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import uk.ac.horizon.artcodes.ArtcodeStorage;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ActionBinding;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.storage.StoreListener;

public class ActionActivity extends AppCompatActivity
{
	public static void start(Context context, Action action)
	{
		Intent intent = new Intent(context, ActionActivity.class);
		intent.putExtra("action", ExperienceParserFactory.toJson(action));
		context.startActivity(intent);
	}

	private ActionBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.action);
		Intent intent = getIntent();
		if (intent.getStringExtra("action") != null)
		{
			ArtcodeStorage.load(Action.class).fromJson(intent.getStringExtra("action")).async(new StoreListener<Action>()
			{
				@Override
				public void onItemChanged(Action item)
				{
					binding.setAction(item);
				}
			});
		}
	}

	public void open(View view)
	{
		if (binding.getAction() != null && binding.getAction().getUrl() != null)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(binding.getAction().getUrl())));
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalytics.trackScreen("Action Screen");
	}
}
