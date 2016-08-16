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

package uk.ac.horizon.artcodes.process;

import android.content.Context;

import org.opencv.core.Core;

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class IntensityFilter implements ImageProcessor
{
    public static class IntensityFilterFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "intensity";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, Map<String, String> args)
        {
            return new IntensityFilter();
        }
    }

    @Override
    public void process(ImageBuffers buffers)
    {
        // ImageBuffers implements this if needed when switching to the grey scale color space from
        // either RGB or YUV.
        buffers.getImageInGrey();
    }

    @Override
    public void getSettings(List<DetectorSetting> settings)
    {
    }
}
