/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.core.activities.ScanActivity;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

public class AestheticodesActivity extends ScanActivity
{
	private Button markerButton;
	private boolean autoOpen = false;
	private String currentCode = null;
	private String experienceURL = null;


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void experienceSelected(Experience experience)
	{
		super.experienceSelected(experience);

		Log.i("", "Selected Experience changed to " + experience.getId());

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayShowTitleEnabled(true);
			getSupportActionBar().setTitle(experience.getName());
			if (experience.getIcon() != null)
			{
//				Ion.with(this).load(experience.getIcon())
//				Aestheticodes.getPicasso(this).load(experience.getIcon()).into(new Target()
//				{
//					@Override
//					public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
//					{
//						getSupportActionBar().setIcon(new BitmapDrawable(getResources(), bitmap));
//					}
//
//					@Override
//					public void onBitmapFailed(Drawable errorDrawable)
//					{
//
//					}
//
//					@Override
//					public void onPrepareLoad(Drawable placeHolderDrawable)
//					{
//
//					}
//				});
			}
		}
	}


	@Override
	protected void onNewIntent(Intent intent)
	{
		if (intent.getAction().equals(Intent.ACTION_VIEW))
		{
			Uri data = intent.getData();
			if (data != null)
			{
				Log.i("", "Intent URL = " + data);
				this.experienceURL = data.toString();
			}
		}
		else
		{
			super.onNewIntent(intent);
		}
	}

	@Override
	public void markerChanged(final String markerCode)
	{
		Log.i("", "Marker changing from " + currentCode + " to " + markerCode);
		final String oldCode = currentCode;
		currentCode = markerCode;

		if (markerCode != null)
		{
			final Marker marker = experience.get().getMarker(markerCode);
			if (autoOpen)
			{
				if (marker != null)
				{
					openMarker(marker);
				}
			}
			else
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (marker != null)
						{
							if (marker.getTitle() != null && !marker.getTitle().isEmpty())
							{
								markerButton.setText(getString(R.string.marker_open, marker.getTitle()));
							}
							else
							{
								markerButton.setText(getString(R.string.marker_open_code, marker.getCode()));
							}
							markerButton.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									openMarker(marker);
								}
							});

							if (oldCode == null)
							{
								Log.i("", "Slide in");
								markerButton.setVisibility(View.VISIBLE);
								markerButton.startAnimation(AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_in));
							}
						}
						//else
						//{
						//markerButton.setText("Unknown Marker " + markerCode);
						//markerButton.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_add_white_36dp, 0);
						//markerButton.setOnClickListener(new View.OnClickListener()
						//{
						//	@Override
						//	public void onClick(View v)
						//	{
						//	}
						//});
						//}
					}
				});
			}
		}
		else if (markerButton.getVisibility() == View.VISIBLE)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Animation animation = AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_out);
					animation.setAnimationListener(new Animation.AnimationListener()
					{
						@Override
						public void onAnimationStart(Animation animation)
						{

						}

						@Override
						public void onAnimationEnd(Animation animation)
						{
							markerButton.setVisibility(View.INVISIBLE);
						}

						@Override
						public void onAnimationRepeat(Animation animation)
						{

						}
					});
					markerButton.startAnimation(animation);
				}
			});
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// TODO View view = getLayoutInflater().inflate(R.layout.marker_button, bottomView);
		//markerButton = (Button) view.findViewById(R.id.markerButton);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setIcon(R.mipmap.ic_launcher);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.home:
				startActivity(new Intent(this, ExperienceListActivity.class));
				return true;
			case R.id.experiences:
				startActivity(new Intent(this, ExperienceListActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		//experiences.addListener(this);
		//experiences.updateView(experienceURL);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		//experiences.removeListener(this);
		if (experience.get() != null)
		{
			getPreferences(Context.MODE_PRIVATE).edit().putString("experience", experience.get().getId()).commit();
		}
	}

	@Override
	protected void updateMenu()
	{
		super.updateMenu();

		Button autoOpenButton = (Button) findViewById(uk.ac.horizon.aestheticodes.core.R.id.autoOpenButton);
		autoOpenButton.setVisibility(View.VISIBLE);
		autoOpenButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				autoOpen = !autoOpen;
				updateMenu();
				if (currentCode != null)
				{
					String code = currentCode;
					currentCode = null;
					markerChanged(code);
				}
			}
		});
		if (autoOpen)
		{
			autoOpenButton.setText(getString(R.string.auto_open));
			autoOpenButton.setCompoundDrawablesWithIntrinsicBounds(uk.ac.horizon.aestheticodes.core.R.drawable.ic_open_in_new_white_24dp, 0, 0, 0);
		}
		else
		{
			autoOpenButton.setText(getString(R.string.auto_open_off));
			autoOpenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_popup_white_24dp, 0, 0, 0);
		}
	}

	private void openMarker(Marker marker)
	{
		camera.stop();
		if (marker.getShowDetail())
		{
			Intent intent = new Intent(this, MarkerActivity.class);
			intent.putExtra("experience", experience.get().getId());
			intent.putExtra("marker", marker.getCode());

			startActivity(intent);
		}
		else
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
		}
	}
}