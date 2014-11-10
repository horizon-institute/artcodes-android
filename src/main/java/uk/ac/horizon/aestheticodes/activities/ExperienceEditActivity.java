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

package uk.ac.horizon.aestheticodes.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.bindings.IntPropertyBinding;
import uk.ac.horizon.aestheticodes.bindings.IntRangePropertyBinding;
import uk.ac.horizon.aestheticodes.bindings.ViewBindings;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;
import uk.ac.horizon.aestheticodes.dialogs.MarkerEditDialog;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExperienceEditActivity extends ActionBarActivity
{
	private Experience experience;
	private LinearLayout markerSettings;
	private ImageView markerSettingsIcon;
	private boolean settingsVisible = false;
	private Animator animator;
	private ViewBindings viewBindings;
	private MenuItem doneAction;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.experience_edit);

		final Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			final String experienceID = extras.getString("experience");
			if (experienceID != null)
			{
				final ExperienceManager experienceManager = ExperienceManager.get(this);
				experience = experienceManager.get(experienceID);
			}
		}

		if(experience == null)
		{
			experience = new Experience();
		}

		viewBindings = new ViewBindings(this, experience)
		{
			@Override
			protected void setValid(boolean valid)
			{
				if (doneAction != null)
				{
					doneAction.setEnabled(valid && experience.hasChanged());
				}
			}
		};

		viewBindings.bind(R.id.experienceTitle, "name");
		viewBindings.bind(R.id.experienceDescription, "description");
		viewBindings.bind(R.id.experienceIcon, "icon");
		viewBindings.bind(R.id.experienceImage, "image");

		viewBindings.bind(R.id.markerRegions, new IntRangePropertyBinding("minRegions", "maxRegions", 1, 9));
		viewBindings.bind(R.id.markerRegionValue, new IntPropertyBinding("maxRegionValue", 1, 9));
		viewBindings.bind(R.id.markerValidationRegions, new IntPropertyBinding("validationRegions", 0, "maxRegions", 0));
		viewBindings.bind(R.id.markerValidationRegionValue, new IntPropertyBinding("validationRegionValue", 1, "maxRegionValue"));
		viewBindings.bind(R.id.markerChecksum, new IntPropertyBinding("checksumModulo", 1, 12, 1));

		markerSettings = (LinearLayout) findViewById(R.id.markerSettings);
		markerSettingsIcon = (ImageView) findViewById(R.id.markerSettingsIcon);

		final LinearLayout markerList = (LinearLayout) findViewById(R.id.markerList);
		final List<MarkerAction> markers = new ArrayList<MarkerAction>(experience.getMarkers().values());
		Collections.sort(markers, new Comparator<MarkerAction>()
		{
			@Override
			public int compare(MarkerAction markerAction, MarkerAction markerAction2)
			{
				return markerAction.getCode().compareTo(markerAction2.getCode());
			}
		});
		LayoutInflater inflater = getLayoutInflater();
		for (final MarkerAction marker : markers)
		{
			View view = inflater.inflate(R.layout.marker_listitem, markerList, false);
			viewBindings.bind(view, R.id.markerCode, marker, "code");
			viewBindings.bind(view, R.id.markerAction, marker, "action");
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					DialogFragment newFragment = new MarkerEditDialog();
					Bundle bundle = new Bundle();
					bundle.putString("code", marker.getCode());
					newFragment.setArguments(bundle);
					newFragment.show(getSupportFragmentManager(), "marker.edit");
				}
			});
			markerList.addView(view);
		}

		markerSettings.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
		{
			@Override
			public boolean onPreDraw()
			{
				markerSettings.getViewTreeObserver().removeOnPreDrawListener(this);
				markerSettings.setVisibility(View.GONE);

				final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
				final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
				markerSettings.measure(widthSpec, heightSpec);

				animator = slideAnimator(0, markerSettings.getMeasuredHeight());
				return true;
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_actions, menu);

		doneAction = menu.findItem(R.id.action_done);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// Respond to the action bar's Up/Home open_button
			case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(Intent.ACTION_EDIT, Uri.parse("aestheticodes://" + experience.getId())));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void toggleMarkerSettings(View view)
	{
		if (settingsVisible)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				int finalHeight = markerSettings.getHeight();
				ValueAnimator animator = slideAnimator(finalHeight, 0);
				animator.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationEnd(Animator animator)
					{
						//Height=0, but it set visibility to GONE
						markerSettings.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationStart(Animator animator)
					{
					}

					@Override
					public void onAnimationCancel(Animator animator)
					{
					}

					@Override
					public void onAnimationRepeat(Animator animator)
					{
					}
				});
				animator.start();
			}
			else
			{
				markerSettings.setVisibility(View.GONE);
			}
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_more_24dp);
		}
		else
		{
			markerSettings.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				animator.start();
			}
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_less_24dp);
		}
		settingsVisible = !settingsVisible;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private ValueAnimator slideAnimator(int start, int end)
	{
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator)
			{
				//Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();

				ViewGroup.LayoutParams layoutParams = markerSettings.getLayoutParams();
				layoutParams.height = value;
				markerSettings.setLayoutParams(layoutParams);
			}
		});
		return animator;
	}

	public void addMarker(View view)
	{
		Log.i(ExperienceActivity.class.getName(), "Add Marker");
		DialogFragment newFragment = new MarkerEditDialog();
		Bundle bundle = new Bundle();
		bundle.putString("marker", "//TODO");
		newFragment.setArguments(bundle);
		newFragment.show(getSupportFragmentManager(), "marker.edit");
	}

	public ViewBindings getViewBindings()
	{
		return viewBindings;
	}

	public Experience getExperience()
	{
		return experience;
	}
}