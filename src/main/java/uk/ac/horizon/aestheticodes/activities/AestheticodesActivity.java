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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.storicodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListAdapter;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.core.activities.ScanActivity;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.Scene;
import uk.ac.horizon.aestheticodes.model.Thumbnail;

public class AestheticodesActivity extends ScanActivity implements ExperienceListController.Listener
{
	private ExperienceListAdapter experiences;
	private Button markerButton;
	private boolean autoOpen = false;
	private String currentCode = null;
	private String experienceURL = null;

	private Animation slideAnimation = null;
	private final Object animationLock = new Object();
	private enum AnimationState {offScreen, slidingIn, onScreen, slidingOut};
	private AnimationState animationState = AnimationState.offScreen;

	private static final Scalar thumbnailColour = new Scalar(255, 255, 0, 255);

	@Override
	public void experienceListChanged()
	{
		if(experienceURL != null)
		{
			for(Experience experience: experiences.getExperiences())
			{
				if(experienceURL.equals(experience.getOrigin()))
				{
					this.experienceController.set(experience);
					return;
				}
			}
		}
		this.loadSelectExperienceFromPreferences();
	}

	private void loadSelectExperienceFromPreferences()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String selectedID = sharedPreferences.getString("experience", experienceController.get().getId());
		Log.i("EXPERIENCE_PREF", "Loading experience from prefs... "+selectedID);
		Experience newSelected = experiences.getSelected(selectedID);
		if (newSelected != null && newSelected != experienceController.get())
		{
			Log.i("EXPERIENCE_PREF", "...found "+newSelected.getName());
			experienceController.set(newSelected);
		}
	}

	private void saveSelectedExperienceToPreferences(Experience experience)
	{
		if(experience != null && experience.getId() != null)
		{
			String experienceId = experience.getId();
			Log.i("EXPERIENCE_PREF", "Setting experience id " + experienceId);
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("experience", experience.getId());
			editor.apply();
		}
	}

	@Override
	public void experienceSelected(final Experience experience)
	{
		super.experienceSelected(experience);

		Log.i("", "Selected Experience changed to " + experience.getId());

		List<Experience> experienceList = experiences.getExperiences();
		int index = experienceList.indexOf(experience);
		//getSupportActionBar().setSelectedNavigationItem(index);
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				getSupportActionBar().setTitle(experience.getName());
			}
		});

		this.saveSelectedExperienceToPreferences(experience);
		this.markerChanged(null, null, -1, null, false);
	}


	@Override
	protected void onNewIntent(Intent intent)
	{
		if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_VIEW))
		{
			Uri data = intent.getData();
			if(data != null)
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
	public void markerChanged(final String markerCode, final List<Integer> newMarkerContourIndexes, final int historySize, final Scene scene, boolean detectionInProgress)
	{
		Log.i(this.getClass().getName(), "Marker changing from " + currentCode + " to " + markerCode);
		final String oldCode = currentCode;
		currentCode = markerCode;

		// create new thumbnails
		final List<Thumbnail> newMarkerThumbnails = new ArrayList<>();
		int thumbnailSizeInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, Resources.getSystem().getDisplayMetrics());
		if (newMarkerContourIndexes!=null)
		{
			for (int newMarkerContourIndex : newMarkerContourIndexes)
			{
				newMarkerThumbnails.add(new Thumbnail(newMarkerContourIndex, scene, -1, thumbnailSizeInPixels, thumbnailColour));
			}
		}

		// update thumbnails
		final LinearLayout historyView = (LinearLayout) findViewById(R.id.historyThumbnails);
		if (!newMarkerThumbnails.isEmpty() || historyView.getChildCount() != historySize)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					for (Thumbnail thumbnail : newMarkerThumbnails)
					{
						ImageView thumbnailView = new ImageView(getApplicationContext());
						thumbnailView.setImageBitmap(thumbnail.getBitmap());
						thumbnailView.setPadding(1, 0, 1, 0);
						historyView.addView(thumbnailView);
					}
					while (historySize>=0 && historyView.getChildCount()>=1 && historyView.getChildCount() > historySize) {
						historyView.removeViewAt(0);
					}
				}
			});
		}

		//
		String hint = null;
		Experience experience = experienceController.get();
		if (historySize > 0)
		{
			if (experience!=null && experience.getHintText()!=null && experience.getHintText().get("sequencePart")!=null)
			{
				hint = experience.getHintText().get("sequencePart");
			}
			else
			{
				hint = "This code is part of a sequence, find the next!";
			}
		}
		else if (markerCode!=null)
		{
			int numberOfCodes = markerCode.split("\\+").length;
			switch (numberOfCodes)
			{
				case 1:
					if (experienceController.get()!=null && experienceController.get().getMarkers().containsKey(markerCode))
					{
						hint = "Found one!";
					}
					else
					{
						hint = "Found one... but not one you were looking for...";
					}
					break;
				case 2:
					hint = "Found two!";
					break;
				case 3:
					hint = "Found three!";
					break;
				default:
					hint = "Found "+numberOfCodes+"!";
					break;
			}
		}
		else if (detectionInProgress)
		{
			hint = "Hold it there!";
		}
		else
		{
			if (experience!=null && experience.getHintText()!=null && experience.getHintText().get("rest")!=null)
			{
				hint = experience.getHintText().get("rest");
			}
			else
			{
				hint = "Place an Artcode in the camera view";
			}
		}
		final String finalHint = new String(hint);
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				modeText.setText(finalHint);
			}
		});




		final Marker marker = experienceController.get().getMarkers().get(markerCode);
		if (marker!=null && ((autoOpen && experienceController.get().getOpenMode()==null) || (experienceController.get().getOpenMode()!=null && experienceController.get().getOpenMode().equals("autoOpen"))))
		{
			openMarker(marker);
		}
		else
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (animationLock)
					{
						// marker action
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

							if (animationState==AnimationState.offScreen || animationState==AnimationState.slidingOut)
							{
								if (slideAnimation != null)
								{
									slideAnimation.cancel();
									slideAnimation = null;
								}
								markerButton.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_in);
								slideAnimation = animation;
								animation.setAnimationListener(new Animation.AnimationListener()
								{
									@Override
									public void onAnimationStart(Animation animation)
									{
									}

									@Override
									public void onAnimationEnd(Animation animation)
									{
										synchronized (animationLock)
										{
											if (animationState==AnimationState.slidingIn)
											{
												animationState = AnimationState.onScreen;
											}
										}
									}

									@Override
									public void onAnimationRepeat(Animation animation)
									{
									}
								});
								animationState = AnimationState.slidingIn;
								markerButton.startAnimation(animation);
							}
						}
						else if (animationState==AnimationState.onScreen || animationState==AnimationState.slidingIn)
						{
							if (slideAnimation != null)
							{
								slideAnimation.cancel();
								slideAnimation = null;
							}

							Animation animation = AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_out);
							slideAnimation = animation;
							animation.setAnimationListener(new Animation.AnimationListener()
							{
								@Override
								public void onAnimationStart(Animation animation)
								{
								}

								@Override
								public void onAnimationEnd(Animation animation)
								{
									synchronized (animationLock)
									{
										if (animationState==AnimationState.slidingOut)
										{
											markerButton.setVisibility(View.INVISIBLE);
											animationState = AnimationState.offScreen;
										}
									}
								}

								@Override
								public void onAnimationRepeat(Animation animation)
								{
								}
							});
							animationState = AnimationState.slidingOut;
							markerButton.startAnimation(animation);
						}
					}
				}
			});
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		View view = getLayoutInflater().inflate(R.layout.marker_button, bottomView);
		markerButton = (Button) view.findViewById(R.id.markerButton);
		
		experiences = new ExperienceListAdapter(getSupportActionBar().getThemedContext(), Aestheticodes.getExperiences());

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		/*
		//noinspection deprecation
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//noinspection deprecation
		getSupportActionBar().setListNavigationCallbacks(experiences, new ActionBar.OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int position, long l)
			{
				final Experience selected = (Experience) experiences.getItem(position);
				Log.i("", "User selected " + selected.getId() + ": " + l);
				if (selected != experienceController.get())
				{
					experienceController.set(selected);
				}
				return true;
			}
		});
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.about:
				Intent intent = new Intent();
				intent.putExtra("caller", this.getClass().getCanonicalName());
				intent.setClass(this, WebActivity.class);
				intent.putExtra("URL", "ABOUT");
				intent.putExtra("experience", experienceController.get().getId());
				this.startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		experiences.addListener(this);
		loadSelectExperienceFromPreferences();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		experiences.removeListener(this);
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
				if(currentCode != null)
				{
					String code = currentCode;
					currentCode = null;
					markerChanged(code, null, -1, null, false);
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
		if (marker != null)
		{
			if (marker.getShowDetail())
			{
				camera.stop();
				Intent intent = new Intent(this, MarkerActivity.class);
				intent.putExtra("experience", experienceController.get().getId());
				intent.putExtra("marker", marker.getCode());

				startActivity(intent);
			}
			else if (marker.getAction() != null && marker.getAction().contains("://"))
			{
				camera.stop();
				Intent intent = new Intent();
				intent.putExtra("caller", this.getClass().getCanonicalName());
				intent.setClass(this, WebActivity.class);
				intent.putExtra("URL", marker.getAction());
				this.startActivity(intent);
			}
			else
			{
				new AlertDialog.Builder(this)
						.setTitle("No action")
						.setMessage("There is no valid action associated with this marker.")
						.setPositiveButton("OK", null)
						.show();
			}

			if (marker.getChangeToExperienceWithIdOnOpen() != null)
			{
				Log.i("EXPERIENCE_PREF", "Changing to experience "+marker.getChangeToExperienceWithIdOnOpen() + "(from marker.getChangeToExperienceOnOpen)");
				Experience experienceToChangeTo = this.experiences.getSelected(marker.getChangeToExperienceWithIdOnOpen());
				if (experienceToChangeTo != null)
				{
					Log.i("EXPERIENCE_PREF", "..." + experienceToChangeTo.getName());
					saveSelectedExperienceToPreferences(experienceToChangeTo);
				}
				else
				{
					Log.i("EXPERIENCE_PREF", "...but it was not found!");
				}
			}
			else
			{
				Log.i("EXPERIENCE_PREF", "getChangeToExperienceOnOpen is null");
			}
		}
	}
}
