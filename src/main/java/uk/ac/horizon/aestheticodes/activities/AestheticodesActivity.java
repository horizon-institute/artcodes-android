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
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListAdapter;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.core.activities.ScanActivity;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

public class AestheticodesActivity extends ScanActivity implements ExperienceListController.Listener
{
	private ExperienceListAdapter experiences;
	private Button markerButton;
	private boolean autoOpen = false;
	private String currentCode = null;
	private String experienceURL = null;

	@Override
	public void experienceListChanged()
	{
		if(experienceURL != null)
		{
			for(Experience experience: experiences.getExperiences())
			{
				if(experienceURL.equals(experience.getOrigin()))
				{
					this.experience.set(experience);
					return;
				}
			}
		}
		String selectedID = getPreferences(Context.MODE_PRIVATE).getString("experience", experience.get().getId());
		Experience newSelected = experiences.getSelected(selectedID);
		if (newSelected != null && newSelected != experience.get())
		{
			experience.set(newSelected);
		}
	}

	@Override
	public void experienceSelected(Experience experience)
	{
		super.experienceSelected(experience);

		Log.i("", "Selected Experience changed to " + experience.getId());

		List<Experience> experienceList = experiences.getExperiences();
		int index = experienceList.indexOf(experience);
		getSupportActionBar().setSelectedNavigationItem(index);

		addDefaultExperiences(Aestheticodes.getExperiences());
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
	public void markerChanged(final String markerCode)
	{
		Log.i("", "Marker changing from " + currentCode + " to " + markerCode);
		final String oldCode = currentCode;
		currentCode = markerCode;

		if (oldCode!=currentCode || (oldCode!=null && !oldCode.equals(currentCode))) {
			final Marker marker = experience.get().getMarkers().get(markerCode);
			if (marker != null) {
				if (autoOpen) {
					openMarker(marker);
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (marker.getTitle() != null && !marker.getTitle().isEmpty()) {
								markerButton.setText(getString(R.string.marker_open, marker.getTitle()));
							} else {
								markerButton.setText(getString(R.string.marker_open_code, marker.getCode()));
							}
							markerButton.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									openMarker(marker);
								}
							});

							if (markerButton.getVisibility() == View.INVISIBLE || markerButton.getAnimation() != null) {
								Log.i("", "Slide in");
								markerButton.setVisibility(View.VISIBLE);
								markerButton.startAnimation(AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_in));
							}
						}
					});
				}
			} else if (markerButton.getVisibility() == View.VISIBLE) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Animation animation = AnimationUtils.loadAnimation(AestheticodesActivity.this, R.anim.slide_out);
						animation.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								markerButton.setVisibility(View.INVISIBLE);
							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}
						});
						markerButton.startAnimation(animation);
					}
				});
			}
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
				if (selected != experience.get())
				{
					experience.set(selected);
				}
				return true;
			}
		});
	}

	private void addDefaultExperiences(ExperienceListController experienceListController)
	{
		try
		{
			String json = "[";
			if (true) // use colour defaults
				json += "{\"name\":\"2.1 Red\",   \"UUID\":\"5a5d7329-a73a-45ac-9066-bdc922c93a66\", \"colourPreset\":[\"RGB\",1,0,0], \"minRegions\":5, \"maxRegions\":6, \"checksum\":3, \"icon\":\"http://www.nottingham.ac.uk/~pszwp/red.gif\",   \"embeddedChecksum\":true}," +
						"{\"name\":\"2.2 Green\", \"UUID\":\"f988f134-780e-4760-8b65-516663c5fab8\", \"colourPreset\":[\"RGB\",0,1,0], \"minRegions\":5, \"maxRegions\":6, \"checksum\":3, \"icon\":\"http://www.nottingham.ac.uk/~pszwp/green.gif\", \"embeddedChecksum\":true}," +
						"{\"name\":\"2.3 Blue\",  \"UUID\":\"3c9833bb-46df-406d-bae6-8d4c0410d02a\", \"colourPreset\":[\"RGB\",0,0,1], \"minRegions\":5, \"maxRegions\":6, \"checksum\":3, \"icon\":\"http://www.nottingham.ac.uk/~pszwp/blue.gif\",  \"embeddedChecksum\":true},";
			if (true) // use extension defaults
				json += "{\"name\":\"1.1 Area Order\", \"UUID\":\"6dd56665-8523-43e8-925d-71d6d94be4be\", \"minRegions\":5, \"maxRegions\":5, \"checksum\":3, \"description\":\"This experience orders the regions of an Artcode by their size. (AREA4321)\", \"icon\":\"http://www.nottingham.ac.uk/~pszwp/extension.gif\", \"version\":2}," +
								"{\"name\":\"1.2 Area Label/Orientation Order\", \"UUID\":\"ce4b84b6-6cfc-4969-a4e4-072334c337b8\", \"minRegions\":5, \"maxRegions\":5, \"checksum\":3, \"embeddedChecksum\":true, \"description\":\"This experience labels the regions of an Artcode by their size and then orders them by their orientation. (AO4321)\", \"icon\":\"http://www.nottingham.ac.uk/~pszwp/extension.gif\", \"version\":2}," +
								"{\"name\":\"1.3 Orientation Label/Area Order\", \"UUID\":\"1d4bac87-e9e3-4e12-8208-34c168922e34\", \"minRegions\":5, \"maxRegions\":5, \"checksum\":3, \"embeddedChecksum\":true, \"description\":\"This experience labels the regions of an Artcode by their orientation and then orders them by their size. (OA4321)\", \"icon\":\"http://www.nottingham.ac.uk/~pszwp/extension.gif\", \"version\":2}," +
								"{\"name\":\"1.4 Touching\", \"UUID\":\"069674f8-3a8b-49bd-aef6-5b0bc6196c67\", \"minRegions\":5, \"maxRegions\":5, \"checksum\":3, \"embeddedChecksum\":true, \"description\":\"This experience counts the number of other regions a region touches. This produces codes like 1-1:1-2:1-2:1-2:2-2 where 1-2 means a region with a value of 1 that is touching 2 other regions. The total of these touching numbers must be disiable by 3. (TOUCH4321)\", \"icon\":\"http://www.nottingham.ac.uk/~pszwp/extension.gif\", \"version\":2}";
			json += "]";
			JSONArray arrayOfDefaultExperences = new JSONArray(json);

			for (int i=0; i<arrayOfDefaultExperences.length(); ++i)
			{
				JSONObject experienceDict = arrayOfDefaultExperences.getJSONObject(i);
				Experience experience = new Experience();
				experience.setId(experienceDict.getString("UUID"));
				experience.setOp(Experience.Operation.create);
				experience.setName(experienceDict.getString("name"));
				if (experienceDict.has("version"))
					experience.setVersion(experienceDict.getInt("version"));

				Experience existingExperience = experienceListController.get(experience.getId());
				if (existingExperience == null || existingExperience.getVersion() < experience.getVersion())
				{
					if (experienceDict.has("icon"))
						experience.setIcon(experienceDict.getString("icon"));
					if (experienceDict.has("description"))
						experience.setDescription(experienceDict.getString("description"));

					if (experienceDict.has("minRegions"))
						experience.setMinRegions(experienceDict.getInt("minRegions"));
					if (experienceDict.has("maxRegions"))
						experience.setMaxRegions(experienceDict.getInt("maxRegions"));
					if (experienceDict.has("checksum"))
						experience.setChecksumModulo(experienceDict.getInt("checksum"));
					if (experienceDict.has("embeddedChecksum"))
						experience.setEmbeddedChecksum(experienceDict.getBoolean("embeddedChecksum"));

					if (experienceDict.has("colourPreset"))
					{
						List<Object> preset = new ArrayList<>();
						JSONArray presetInJson = experienceDict.getJSONArray("colourPreset");
						preset.add(presetInJson.getString(0));
						for (int j = 1; j < presetInJson.length(); ++j)
							preset.add(presetInJson.getDouble(j));
						experience.setGreyscaleOptions(preset);
					}
					if (experienceDict.has("invertGreyscale"))
						experience.setInvertGreyscale(experienceDict.getBoolean("invertGreyscale"));
					if (experienceDict.has("hueShift"))
						experience.setHueShift(experienceDict.getDouble("hueShift"));

					experienceListController.add(experience);
				}
			}

		} catch (JSONException e)
		{
			e.printStackTrace();
		}
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
		experiences.addListener(this);
		experiences.update(experienceURL);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		experiences.removeListener(this);
		if(experience.get() != null)
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
				if(currentCode != null)
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
