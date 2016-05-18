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

package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceHistoryActivity extends ExperienceActivityBase
{
	public static void start(Context context, Experience experience)
	{
		Intent intent = new Intent(context, ExperienceHistoryActivity.class);
		intent.putExtra("experience", new Gson().toJson(experience));
		context.startActivity(intent);
	}

	@Override
	public void loaded(Experience experience)
	{
		// TODO
		super.loaded(experience);
	}


}
