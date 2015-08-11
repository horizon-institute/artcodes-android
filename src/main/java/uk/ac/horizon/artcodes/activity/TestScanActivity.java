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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;

import java.net.URLEncoder;

public class TestScanActivity extends Activity
{
	private static final int SCAN_REQUEST = 113;
	private TextView label;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		LinearLayout layout = new LinearLayout(this);

		Button button1 = new Button(this);
		button1.setText("Scan Action");
		button1.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent ai = new Intent("uk.ac.horizon.aestheticodes.SCAN");
				ai.putExtra("experience", experienceToJson(createExperience()));
				startActivityForResult(ai, SCAN_REQUEST);
			}
		});
		layout.addView(button1);

		Button button2 = new Button(this);
		button2.setText("Scan URL");
		button2.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					Experience experience = createExperience();
					experience.setCallback("http://aestheticodes.appspot.com/action/{code}");
					String url = getString(R.string.artcode_scan_scheme) + ":" + URLEncoder.encode(experienceToJson(experience), "UTF-8");
					Log.i("", url);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
		});
		layout.addView(button2);

		Button button3 = new Button(this);
		button3.setText("Add Experience");
		button3.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					String url = "https://aestheticodes.appspot.com/experience/b29f7f6a-7fdd-4a6d-bee3-3f27f77ef931.artcode";
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
		});
		layout.addView(button3);

		label = new TextView(this);
		layout.addView(label);

		setContentView(layout);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_CANCELED)
		{
			label.setText("scan cancelled");
		}
		else if (resultCode == Activity.RESULT_OK)
		{
			String marker = data.getStringExtra("action");
			if (marker == null)
			{
				label.setText("scan result for null action");
			}
			else
			{
				label.setText("Found Marker " + marker);
			}
		}
	}

	private Experience createExperience()
	{
		Experience experience = new Experience();
		experience.setName("Test");
		return experience;
	}

	private String experienceToJson(Experience experience)
	{
		final Gson gson = new GsonBuilder().create();
		return gson.toJson(experience);
	}
}
