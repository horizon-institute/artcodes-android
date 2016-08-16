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

package uk.ac.horizon.artcodes.detect;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.opencv.core.Rect;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.horizon.artcodes.detect.marker.MarkerEmbeddedChecksumAreaOrderDetector;
import uk.ac.horizon.artcodes.detect.marker.MarkerAreaOrderDetector;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetector;
import uk.ac.horizon.artcodes.detect.marker.MarkerEmbeddedChecksumDetector;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.CmykColourFilter;
import uk.ac.horizon.artcodes.process.HlsEditImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;
import uk.ac.horizon.artcodes.process.IntensityFilter;
import uk.ac.horizon.artcodes.process.Inverter;
import uk.ac.horizon.artcodes.process.RgbColourFilter;
import uk.ac.horizon.artcodes.process.TileThresholder;
import uk.ac.horizon.artcodes.process.WhiteBalanceImageProcessor;

public class ArtcodeDetector extends Detector
{
	private static final Map<String, ImageProcessorFactory> factoryRegistry = new HashMap<>();

	static
	{
		register(new MarkerDetector.Factory());
		register(new MarkerEmbeddedChecksumDetector.Factory());
		register(new MarkerAreaOrderDetector.Factory());
		register(new MarkerEmbeddedChecksumAreaOrderDetector.Factory());
		
		register(new TileThresholder.Factory());

		register(new IntensityFilter.IntensityFilterFactory());
		register(new Inverter.InverterFactory());
		register(new WhiteBalanceImageProcessor.WhiteBalanceImageProcessorFactory());
		register(new HlsEditImageProcessor.HlsEditImageProcessorFactory());
		register(new RgbColourFilter.RedFactory());
		register(new RgbColourFilter.GreenFactory());
		register(new RgbColourFilter.BlueFactory());
		register(new CmykColourFilter.CyanCmykColourFilterFactory());
		register(new CmykColourFilter.MagentaCmykColourFilterFactory());
		register(new CmykColourFilter.YellowCmykColourFilterFactory());
		register(new CmykColourFilter.BlackCmykColourFilterFactory());
	}

	public ArtcodeDetector(final Context context, Experience experience, MarkerDetectionHandler handler)
	{
		boolean missingProcessors = false;

		for (String processorName : experience.getPipeline())
		{
			ImageProcessor processor = getProcessor(context, processorName, experience, handler);
			if (processor != null)
			{
				pipeline.add(processor);
			}
			else
			{
				missingProcessors = true;
			}
		}

		if (missingProcessors)
		{
			new AlertDialog.Builder(context)
					.setTitle("Hmm...")
					.setMessage("This experience uses features not in this version of Artcodes. It might work fine or you can check Google Play for updates.")
					.setPositiveButton("Update", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final String appPackageName = context.getPackageName();
							try {
								context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
							} catch (android.content.ActivityNotFoundException e) {
								context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
							}
						}
					})
					.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();

		}

		if (pipeline.isEmpty())
		{
			pipeline.add(new TileThresholder());
			pipeline.add(new MarkerDetector(context, experience, handler));
		}
	}

	private static void register(ImageProcessorFactory factory)
	{
		factoryRegistry.put(factory.getName(), factory);
	}

	private static ImageProcessor getProcessor(Context context, String string, Experience experience, MarkerDetectionHandler handler)
	{
		// matches "group1" or "group1(group2)" or "group1()"
		String pattern = "([^\\(\\)]+)(?:\\(([^\\(\\)]*)\\))?";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(string);

		if (m.find())
		{
			String imageProcessorName = m.group(1);
			String imageProcessorArgs = m.group(2);
			Log.i("ArtcodeDetector", "Attempting to create image processor '"+imageProcessorName+"' with args '"+imageProcessorArgs+"'");
			ImageProcessorFactory factory = factoryRegistry.get(imageProcessorName);
			if (factory != null)
			{
				try
				{
					return factory.create(context, experience, handler, getImageProcessorArgs(imageProcessorArgs));
				}
				catch (Exception e)
				{
					Log.w("detector", e.getMessage(), e);
				}
			}
		}
		else
        {
            Log.i("ArtcodeDetector", "Regex error: '"+pattern+"' did not match '"+string+"'");
        }

		return null;
	}

	/**
	 * Creates a Map from the input.
	 * @param fromString A string of key value pairs (note keys don't need values) in format: "key1=value1,key2,key3=value3".
	 * @return A Map
     */
	private static Map<String,String> getImageProcessorArgs(String fromString)
	{
		Map<String,String> imageProcessorArgs = new HashMap<>();
		if (fromString != null)
		{
			String[] args = fromString.split(",");
			for (String arg : args)
			{
				String[] argArray = arg.trim().split("=");
				if (argArray.length==1)
				{
					imageProcessorArgs.put(argArray[0], argArray[0]);
				}
				else if (argArray.length>=2)
				{
					imageProcessorArgs.put(argArray[0].trim(), argArray[1].trim());
				}
			}
		}
		return imageProcessorArgs;
	}

	@Override
	protected Rect createROI(int imageWidth, int imageHeight, int surfaceWidth, int surfaceHeight)
	{
		final int size = Math.min(imageWidth, imageHeight);

		final int colStart = (imageWidth - size) / 2;
		final int rowStart = (imageHeight - size) / 2;

		final float surfaceMax = Math.max(surfaceHeight, surfaceWidth);

		float sizeRatio = surfaceMax / Math.max(imageWidth, imageHeight);
		Log.i("Detector", "Size ratio = " + sizeRatio);

		if(callback != null)
		{
			int margin = (int) (Math.max(colStart, rowStart) * sizeRatio);
			Log.i("Detector", "Size = " + size + ", margin = " + margin);
			callback.detectionStart(margin);
		}
		return new Rect(colStart, rowStart, size, size);
	}
}