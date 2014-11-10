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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.bindings.ViewBindings;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;

public class ExperienceActivity extends ActionBarActivity
{
	private Experience experience;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		String experienceID = extras.getString("experience");

		final ExperienceManager experienceManager = ExperienceManager.get(this);
		experience = experienceManager.get(experienceID);

		setContentView(R.layout.experience);

		final ViewBindings viewBindings = new ViewBindings(this, experience);
		viewBindings.bind(R.id.experienceTitle, "name");
		viewBindings.bind(R.id.experienceDescription, "description");
		viewBindings.bind(R.id.experienceIcon, "icon");
		viewBindings.bind(R.id.experienceImage, "image");

		final ImageButton imageButton = (ImageButton) findViewById(R.id.experienceFloatingAction);
		if (!experience.isEditable())
		{
			imageButton.setVisibility(View.GONE);
		}
	}

	public void editExperience(View view)
	{
		Intent intent = new Intent(ExperienceActivity.this, ExperienceEditActivity.class);
		intent.putExtra("experience", experience.getId());

		startActivity(intent);
	}
}