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

package uk.ac.horizon.artcodes.scanner;

import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.text.TextWatcher;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.Marker;
import uk.ac.horizon.artcodes.scanner.BR;
import uk.ac.horizon.artcodes.scanner.marker.MarkerCode;
import uk.ac.horizon.artcodes.scanner.marker.MarkerCodeFactory;
import uk.ac.horizon.artcodes.scanner.marker.MarkerDrawer;
import uk.ac.horizon.artcodes.scanner.threshold.TemporalTileThresholder;
import uk.ac.horizon.artcodes.scanner.threshold.Thresholder;
import uk.ac.horizon.artcodes.scanner.camera.FrameProcessor;

import java.util.Iterator;
import java.util.List;

public class ExperienceScanner extends Experience
{
	private enum DrawMarker
	{
		off, outline, regions
	}

	private enum DrawThreshold
	{
		off, on
	}

	private DrawMarker drawMarker = DrawMarker.off;
	private MarkerDrawer markerDrawer;
	private DrawThreshold drawThreshold = DrawThreshold.off;
	private Marker marker;
	private Thresholder thresholder = new TemporalTileThresholder();
	private MarkerCodeFactory factory = new MarkerCodeFactory();


	public TextWatcher getDescriptionWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public void onTextChanged(String text)
			{
				if (!getDescription().equals(text))
				{
					setDescription(text);
					notifyPropertyChanged(BR.description);
				}
			}
		};
	}

	@Bindable
	public int getDrawMarkerIcon()
	{
		if (drawMarker == DrawMarker.outline)
		{
			return R.drawable.ic_border_outer_white_24dp;
		}
		else if (drawMarker == DrawMarker.regions)
		{
			return R.drawable.ic_border_all_white_24dp;
		}
		return R.drawable.ic_border_clear_white_24dp;
	}

	@Bindable
	public Integer getDrawMarkerText()
	{
		if (drawMarker == DrawMarker.outline)
		{
			return R.string.draw_marker_outline;
		}
		else if (drawMarker == DrawMarker.regions)
		{
			return R.string.draw_marker_regions;
		}
		return R.string.draw_marker_off;
	}

	@Bindable
	public Integer getDrawThresholdText()
	{
		if (drawThreshold == DrawThreshold.on)
		{
			return R.string.draw_threshold_on;
		}
		return R.string.draw_threshold_off;
	}

	@Bindable
	public int getDrawThresholdIcon()
	{
		if (drawThreshold == DrawThreshold.on)
		{
			return R.drawable.ic_filter_b_and_w_white_24dp;
		}
		return R.drawable.ic_filter_b_and_w_off_white_24dp;
	}


	public FrameProcessor getFrameProcessor()
	{
		return new FrameProcessor()
		{
			private int framesSinceLastMarker = 0;
			private Mat drawImage;
			private Bitmap result;

			@Override
			public void processFrame(Mat frame)
			{
				try
				{
					//Log.i("", "Processing Frame");
					// Cut down region for detection
					Mat croppedImage = crop(frame);

					// apply threshold.
					thresholder.threshold(croppedImage, framesSinceLastMarker);

					if (drawMarker != DrawMarker.off || drawThreshold != DrawThreshold.off)
					{
						rotate(croppedImage, croppedImage);

						if (drawImage == null)
						{
							drawImage = new Mat(croppedImage.rows(), croppedImage.cols(), CvType.CV_8UC4);
						}

						if (drawThreshold == DrawThreshold.on)
						{
							Imgproc.cvtColor(croppedImage, drawImage, Imgproc.COLOR_GRAY2BGRA);
						}
						else
						{
							drawImage.setTo(new Scalar(0, 0, 0));
						}
					}
					else if (drawImage != null)
					{
						drawImage.release();
						drawImage = null;
						result = null;
					}

					// find markers.
					List<MarkerCode> markers = factory.findMarkers(croppedImage, drawImage, ExperienceScanner.this);
					if (markers.size() == 0)
					{
						++framesSinceLastMarker;
					}
					else
					{
						framesSinceLastMarker = 0;
					}

					if (drawImage == null)
					{
						result = null;
					}
					else
					{
						if (result == null)
						{
							result = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
						}
						Utils.matToBitmap(drawImage, result);
					}

					//listener.resultUpdated(!markers.isEmpty(), result);
					//resultCode = markerSelection.addMarkers(markers);
					//notifyPropertyChanged();

				}
				catch (Exception e)
				{
					Log.e("", e.getMessage(), e);
				}

				// Test if camera needs to be focused
//				if (CameraController.deviceNeedsManualAutoFocus && framesSinceLastMarker > 2 && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000)
//				{
//					timeOfLastAutoFocus = System.currentTimeMillis();
//					camera.performManualAutoFocus(new Camera.AutoFocusCallback()
//					{
//						@Override
//						public void onAutoFocus(boolean b, Camera camera)
//						{
//						}
//					});
//				}
			}
		};
	}

	@Bindable
	public Marker getMarker()
	{
		return marker;
	}

	public TextWatcher getNameWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public void onTextChanged(String text)
			{
				if (!getName().equals(text))
				{
					setName(text);
					notifyPropertyChanged(BR.name);
				}
			}
		};
	}

	@Bindable
	public Integer getStatusText()
	{
		return R.string.mode_active_detect;
	}

	public void nextDrawMarker()
	{
		final DrawMarker[] drawMarkers = DrawMarker.values();
		drawMarker = drawMarkers[(drawMarker.ordinal() + 1) % drawMarkers.length];
		notifyPropertyChanged(BR.drawMarkerText);
	}

	public void nextThreshold()
	{
		final DrawThreshold[] drawThresholds = DrawThreshold.values();
		drawThreshold = drawThresholds[(drawThreshold.ordinal() + 1) % drawThresholds.length];
		notifyPropertyChanged(BR.drawThresholdText);
	}

	private void setMarkerCode(String code)
	{
		Marker newMarker = getMarker(code);
		if (newMarker != marker)
		{
			marker = newMarker;
			notifyPropertyChanged(BR.marker);
		}
	}
}
