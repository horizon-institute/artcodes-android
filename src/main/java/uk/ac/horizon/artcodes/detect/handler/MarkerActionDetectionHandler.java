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

package uk.ac.horizon.artcodes.detect.handler;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.horizon.artcodes.detect.marker.Marker;
import uk.ac.horizon.artcodes.drawer.MarkerDrawer;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.MarkerImage;

public class MarkerActionDetectionHandler extends MarkerCodeDetectionHandler
{
	protected static final int REQUIRED = 5;
	protected static final int MAX = REQUIRED;
	// Keep displayed for 10s
	private static final int REMAIN = 10000;

	private final ActionDetectionHandler markerActionHandler;
	private final Experience experience;
	private final MarkerDrawer markerDrawer;
	private Action currentAction;
	private long lastSeen;

	public MarkerActionDetectionHandler(ActionDetectionHandler markerActionHandler, Experience experience, MarkerDrawer markerDrawer)
	{
		super(experience, null);
		this.markerActionHandler = markerActionHandler;
		this.experience = experience;
		this.markerDrawer = markerDrawer;
	}

	@Override
	public void onMarkersDetected(Collection<Marker> markers, ArrayList<MatOfPoint> contours, Mat hierarchy, Size sourceImageSize)
	{
		countMarkers(markers);
		long now = System.currentTimeMillis();
		int best = 0;
		Action selected = null;
		for (Action action : this.experience.getActions())
		{
			if (action.getMatch() == Action.Match.any)
			{
				for (String code : action.getCodes())
				{
					int count = markerCounts.count(code);
					if (count > best)
					{
						selected = action;
						best = count;
					}
				}
			}
			else if (action.getMatch() == Action.Match.all)
			{
				int min = MAX;
				int total = 0;
				for (String code : action.getCodes())
				{
					int count = markerCounts.count(code);
					min = Math.min(min, count);
					total += (count * 2);
				}

				if (min > REQUIRED && total > best)
				{
					best = total;
					selected = action;
				}
			}
		}

		if (best < REQUIRED)
		{
			if (currentAction != null)
			{
				if(now - lastSeen > REMAIN)
				{
					currentAction = null;
					this.markerActionHandler.onMarkerActionDetected(null, null, null);
				}
			}
		}
		else if (selected != currentAction)
		{
			currentAction = selected;
			lastSeen = now;
			ArrayList<MarkerImage> markerImages = null;
			if (this.markerDrawer != null)
			{
				Marker markerObject = null;
				for (Marker possibleMarkerObject : markers)
				{
					if (possibleMarkerObject.toString().equals(currentAction.getCodes().get(0)))
					{
						markerObject = possibleMarkerObject;
					}
				}
				if (markerObject != null)
				{
					final Rect boundingRect = Imgproc.boundingRect(contours.get(markerObject.markerIndex));
					Mat thumbnailMat = this.markerDrawer.drawMarker(markerObject, contours, hierarchy, boundingRect, null);
					Bitmap thumbnail = Bitmap.createBitmap(thumbnailMat.width(), thumbnailMat.height(), Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(thumbnailMat, thumbnail);
					MarkerImage markerImage = new MarkerImage(markerObject.toString(), thumbnail, (float) (boundingRect.tl().x / sourceImageSize.width), (float) (boundingRect.tl().y / sourceImageSize.height), (float) (boundingRect.width / sourceImageSize.width), (float) (boundingRect.height / sourceImageSize.height));
					markerImages = new ArrayList<>(1);
					markerImages.add(markerImage);

					Log.i("SOURCEIMG", "w" + sourceImageSize.width + " h" + sourceImageSize.height);
				}
			}
			this.markerActionHandler.onMarkerActionDetected(currentAction, currentAction, markerImages);
		}
		else
		{
			for (Marker possibleMarkerObject : markers)
			{
				String marker = possibleMarkerObject.toString();
				for(String code: currentAction.getCodes())
				{
					if(code.equals(marker))
					{
						lastSeen = now;
						return;
					}
				}
			}
		}
	}
}
