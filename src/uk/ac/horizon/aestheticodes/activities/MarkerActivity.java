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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.MarkerSettings;

public class MarkerActivity extends FragmentActivity
{
	private MarkerAction marker;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String markerCode = extras.getString("marker");

			setContentView(R.layout.activity_marker);

			marker = MarkerSettings.getSettings().getMarkers().get(markerCode);

			Bitmap image = extras.getParcelable("image");
			ImageView imageView = (ImageView) findViewById(R.id.imageView);
			if(marker.getImage() != null)
			{

				if(image != null)
				{
					BitmapDrawable drawable = new BitmapDrawable(getResources(), image);
					Picasso.with(this).load(marker.getImage()).placeholder(drawable).into(imageView);
				}
				Picasso.with(this).load(marker.getImage()).placeholder(R.drawable.placeholder).into(imageView);
			}
			else if(image != null)
			{
				imageView.setImageBitmap(image);
			}

			TextView titleView = (TextView)findViewById(R.id.titleView);
			if(marker.getTitle() != null)
			{
				titleView.setText(marker.getTitle());
			}
			else
			{
				titleView.setText("Marker " + marker.getCode());
			}

			TextView descriptionView = (TextView)findViewById(R.id.descriptionView);
			if(marker.getDescription() != null)
			{
				descriptionView.setText(marker.getDescription());
			}
			else
			{
				descriptionView.setText(marker.getAction());
			}

			Button button = (Button)findViewById(R.id.button);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view)
				{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
				}
			});

		}



	}

}
