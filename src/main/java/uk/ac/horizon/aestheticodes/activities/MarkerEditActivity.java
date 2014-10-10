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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

public class MarkerEditActivity extends FormActivity
{
	private static class MarkerCodeInputFilter implements InputFilter
	{
		private final Experience experience;

		public MarkerCodeInputFilter(Experience experience)
		{
			this.experience = experience;
		}

		@Override
		public CharSequence filter(CharSequence source, int sourceStart, int sourceEnd, Spanned destination, int destinationStart, int destinationEnd)
		{
			String sourceValue = source.subSequence(sourceStart, sourceEnd).toString();
			if (sourceValue.equals(" "))
			{
				sourceValue = ":";
			}

			String result = destination.subSequence(0, destinationStart).toString() + sourceValue +
					destination.subSequence(destinationEnd, destination.length()).toString();
			if (result.equals(""))
			{
				return sourceValue;
			}
			boolean resultValid = experience.isValidMarker(result, true);

			if (!resultValid && !sourceValue.startsWith(":"))
			{
				sourceValue = ":" + sourceValue;
				resultValid = experience.isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
						destination.subSequence(destinationEnd, destination.length()).toString(), true);
			}

			if (resultValid && !source.subSequence(sourceStart, sourceEnd).toString().equals(sourceValue))
			{
				return sourceValue;
			}

			if (resultValid)
			{
				return null;
			}
			return "";
		}
	}

	private Experience experience;
	private MenuItem doneItem;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_marker_edit);

		Bundle extras = getIntent().getExtras();
		String experienceID = extras.getString("experience");
		String markerCode = extras.getString("code");

		ExperienceManager experienceManager = ExperienceManager.get(this);
		experience = experienceManager.get(experienceID);

		final MarkerAction marker;
		if (markerCode == null || markerCode.isEmpty())
		{
			marker = new MarkerAction();
			String code = experience.getNextUnusedMarker();
			if (code == null)
			{
				// Exit?
			}
			marker.setCode(code);
		}
		else
		{
			marker = experience.getMarkers().get(markerCode);
		}

		final EditText editCode = (EditText) findViewById(R.id.editCode);
		editCode.setText(marker.getCode());
		editCode.setEnabled(markerCode == null);
		editCode.setFilters(new InputFilter[]{new MarkerCodeInputFilter(experience)});
		addValidator(new FieldValidator(editCode)
		{
			@Override
			protected boolean isValid()
			{
				if (experience.isValidMarker(field.getText().toString(), false))
				{
					field.setError(null);
				}
				else
				{
					field.setError("This code is not valid");
					return false;
				}

				if (field.getText().toString().equals(marker.getCode()) || !experience.getMarkers().containsKey(field.getText().toString()))
				{
					field.setError(null);
				}
				else
				{
					field.setError("The code already exists");
					return false;
				}

				return true;
			}

			@Override
			protected void save(String value)
			{
				marker.setCode(value);
			}
		});

		final EditText editTitle = (EditText) findViewById(R.id.editTitle);
		editTitle.setText(marker.getTitle());
		addValidator(new BasicFieldValidator(editTitle)
		{
			@Override
			protected void save(String value)
			{
				marker.setTitle(value);
			}
		});

		final ImageView image = (ImageView) findViewById(R.id.imageView);

		final EditText editImage = (EditText) findViewById(R.id.editImage);
		editImage.setText(marker.getImage());
		addValidator(new FieldValidator(editImage)
		{
			@Override
			protected boolean isValid()
			{
				if (Patterns.WEB_URL.matcher(field.getText().toString()).matches())
				{
					Picasso.with(MarkerEditActivity.this).load(field.getText().toString()).error(R.drawable.aestheticodes).into(image);
					return true;
				}
				field.setError("Invalid URL");
				return false;
			}

			@Override
			protected void save(String value)
			{
				marker.setDescription(value);
			}
		});

		final EditText editDescription = (EditText) findViewById(R.id.editDescription);
		editDescription.setText(marker.getDescription());
		addValidator(new BasicFieldValidator(editDescription)
		{
			@Override
			protected void save(String value)
			{
				marker.setDescription(value);
			}
		});

		final EditText editAction = (EditText) findViewById(R.id.editAction);
		editAction.setText(marker.getAction());
		addValidator(new URLFieldValidator(editAction)
		{
			@Override
			protected void save(String value)
			{
				marker.setAction(value);
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (experience.getIcon() != null)
		{
			Picasso.with(this).load(experience.getIcon()).into(new ActionBarTarget(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_actions, menu);

		doneItem = menu.findItem(R.id.action_done);

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
			case R.id.action_done:
				save();
				NavUtils.navigateUpTo(this, new Intent(Intent.ACTION_EDIT, Uri.parse("aestheticodes://" + experience.getId())));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void setValid(boolean valid)
	{
		doneItem.setEnabled(valid);
	}
}
