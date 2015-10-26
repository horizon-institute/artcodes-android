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

package uk.ac.horizon.aestheticodes.grey;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class runs operations ({@link ImageProcessingComponent}s) over an image
 * (a {@link BufferManager}).
 * Segment of the image can be processed in series and/or parallel (creating its own
 * {@link ExecutorService}) depending on the {@link ImageProcessingComponent}.
 * Use the static method <code>ImageProcessor.parseComponents(List&lt;Sting&gt;)</code> to create a
 * list of {@link ImageProcessingComponent}s for creation.
 */
public class ImageProcessor
{
    private List<List<ImageProcessingComponent>> components = new ArrayList<>();

    private ExecutorService threadPool;

    private List<ImageProcessorTask> imageProcessorTasks;
    private int numberOfImageProcessorTasks = 2;

    /**
     * Create a list of lists of {@link ImageProcessingComponent}s. The order remains the same as
     * input but separated into consecutive lists of {@link ImageProcessingComponent}s that should
     * (or can) be executed in series or parallel over segments of an image. <br>
     * E.g. ["A","B","C","D"] -> [[A],[B,C],[D]] where A & D should be executed over a whole image on a
     * single thread and B & C can be executed on segments of an image on different threads
     * concurrently. <br><br>
     * Recognized input strings include:
     * <ul>
     *     <li>whiteBalance</li>
     *     <li>hlsEdit(X,X,X)</li>
     *     <li>invert</li>
     *     <li>redRgbFilter</li>
     *     <li>greenRgbFilter</li>
     *     <li>blueRgbFilter</li>
     *     <li>cyanCmyFilter</li>
     *     <li>magentaCmyFilter</li>
     *     <li>yellowCmyFilter</li>
     *     <li>cyanCmykFilter</li>
     *     <li>magentaCmykFilter</li>
     *     <li>yellowCmykFilter</li>
     *     <li>blackCmykFilter</li>
     * </ul>
     * @param strings
     * @return
     */
    public static List<List<ImageProcessingComponent>> parseComponents(final List<String> strings)
    {
        List<List<ImageProcessingComponent>> result = new ArrayList<>();
        List<ImageProcessingComponent> currentSegment = null;
        if (strings!=null)
        {
            for (String s : strings)
            {
                ImageProcessingComponent component = null;
                if (s.equalsIgnoreCase("whiteBalance"))
                {
                    component = new WhiteBalanceComponent();
                }
                else if (s.startsWith("hlsEdit"))
                {
                    Pattern p = Pattern.compile("hlsEdit\\(([0-9]+),([0-9]+),([0-9]+)\\)");
                    Matcher m = p.matcher(s);
                    if (m.matches() && m.groupCount() == 3)
                    {
                        component = new HlsEditComponent(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                    }
                    else
                    {
                        Log.w(ImageProcessor.class.getSimpleName(), "Could not parse HLS image processor '" + s + "', ignoring. matches="+m.matches()+" gc="+m.groupCount());
                    }
                }
                else if (s.equalsIgnoreCase("invert"))
                {
                    component = new InvertComponent();
                }
                else if (s.equalsIgnoreCase("redRgbFilter"))
                {
                    component = new RgbComponent(RgbComponent.RgbChannel.red);
                }
                else if (s.equalsIgnoreCase("greenRgbFilter"))
                {
                    component = new RgbComponent(RgbComponent.RgbChannel.green);
                }
                else if (s.equalsIgnoreCase("blueRgbFilter"))
                {
                    component = new RgbComponent(RgbComponent.RgbChannel.blue);
                }
                else if (s.equalsIgnoreCase("cyanCmyFilter"))
                {
                    component = new CmyComponent(CmyComponent.CmyChannel.cyan);
                }
                else if (s.equalsIgnoreCase("magentaCmyFilter"))
                {
                    component = new CmyComponent(CmyComponent.CmyChannel.magenta);
                }
                else if (s.equalsIgnoreCase("yellowCmyFilter"))
                {
                    component = new CmyComponent(CmyComponent.CmyChannel.yellow);
                }
                else if (s.equalsIgnoreCase("cyanCmykFilter"))
                {
                    component = new CmykComponent(CmykComponent.CmykChannel.cyan);
                }
                else if (s.equalsIgnoreCase("magentaCmykFilter"))
                {
                    component = new CmykComponent(CmykComponent.CmykChannel.magenta);
                }
                else if (s.equalsIgnoreCase("yellowCmykFilter"))
                {
                    component = new CmykComponent(CmykComponent.CmykChannel.yellow);
                }
                else if (s.equalsIgnoreCase("blackCmykFilter"))
                {
                    component = new CmykComponent(CmykComponent.CmykChannel.black);
                }
                else
                {
                    Log.w(ImageProcessor.class.getSimpleName(), "Unknown image processor '" + s + "', ignoring.");
                }

                if (component!=null)
                {
                    if (currentSegment == null ||
                            (currentSegment.get(0).segmentRecommended() && !component.segmentSafe()) ||
                            (!currentSegment.get(0).segmentRecommended() && component.segmentRecommended()))
                    {
                        currentSegment = new ArrayList<>();
                        result.add(currentSegment);
                        currentSegment.add(component);
                    }
                    else
                    {
                        currentSegment.add(component);
                    }
                }

            }
        }

        return result;
    }

    public ImageProcessor(List<List<ImageProcessingComponent>> components, int numberOfConcurrentOperations)
    {
        this.components = components;
        this.numberOfImageProcessorTasks = numberOfConcurrentOperations;

        boolean concurrentRecommended = false;
        if (this.components != null)
        {
            for (List<ImageProcessingComponent> segment : this.components)
            {
                if (segment!=null && !segment.isEmpty())
                {
                    concurrentRecommended |= segment.get(0).segmentRecommended();
                }
            }
        }

        if (concurrentRecommended && numberOfConcurrentOperations > 1)
        {
            this.threadPool = Executors.newFixedThreadPool(numberOfConcurrentOperations);
        }
    }

    public void process(BufferManager bufferManager)
    {
        if (this.components!=null)
        {
            Log.i(this.getClass().getSimpleName(), "Processing "+this.components.size()+" segments:");
            long startAll = System.currentTimeMillis();
            for (List<ImageProcessingComponent> segment : this.components)
            {
                if (segment.get(0).segmentRecommended() && threadPool!=null)
                {
                    // multi-threaded segment
                    long start = System.currentTimeMillis();
                    String components = new String();
                    for (ImageProcessingComponent component : segment)
                    {
                        components += component.getClass().getSimpleName() + ", ";
                    }

                    if (imageProcessorTasks==null)
                    {
                        imageProcessorTasks = new ArrayList<>(numberOfImageProcessorTasks);
                        for (int i=0; i<numberOfImageProcessorTasks; ++i)
                        {
                            imageProcessorTasks.add(new ImageProcessorTask());
                        }
                    }

                    List<BufferManager> buffers = bufferManager.getSegments(numberOfImageProcessorTasks);

                    for (int i=0; i<numberOfImageProcessorTasks; ++i)
                    {
                        ImageProcessorTask task = imageProcessorTasks.get(i);
                        task.setComponents(segment);
                        task.setBufferManager(buffers.get(i));
                        threadPool.execute(task);
                    }

                    // Wait for the tasks to be completed.
                    for (ImageProcessorTask task : imageProcessorTasks)
                    {
                        task.waitForTask();
                    }
                    bufferManager.collapseSegmentMetadata();

                    long end = System.currentTimeMillis();
                    Log.i(this.getClass().getSimpleName(), " - Processing components [" + components + "] took " + (end - start) + "ms");
                }
                else
                {
                    // single threaded segment
                    for (ImageProcessingComponent component : segment)
                    {
                        long start = System.currentTimeMillis();
                        component.process(bufferManager);
                        long end = System.currentTimeMillis();
                        Log.i(this.getClass().getSimpleName(), " - Processing component '" + component.getClass().getSimpleName() + "' took " + (end - start) + "ms");
                    }
                }
            }
            long endAll = System.currentTimeMillis();
            Log.i(this.getClass().getSimpleName(), "Total processing took " + (endAll - startAll) + "ms");
        }
        else
        {
            Log.i(this.getClass().getSimpleName(), "Processing components are null.");
        }
    }

    public void release()
    {
        if (this.components!=null)
        {
            for (List<ImageProcessingComponent> segment : this.components)
            {
                for (ImageProcessingComponent component : segment)
                {
                    component.release();
                }
            }
        }
        if (threadPool!=null)
        {
            threadPool.shutdown();
        }
    }

    protected static class ImageProcessorTask implements Runnable
    {
        private BufferManager bufferManager;
        private List<ImageProcessingComponent> components;

        private boolean done = false;

        public void setComponents(List<ImageProcessingComponent> components)
        {
            this.components = components;
            this.done = false;
        }

        public void setBufferManager(BufferManager bufferManager)
        {
            this.bufferManager = bufferManager;
            this.done = false;
        }

        public void run()
        {
            for (ImageProcessingComponent component : this.components)
            {
                component.process(this.bufferManager);
            }

            synchronized (this)
            {
                this.done = true;

                this.notifyAll();
            }
        }

        public synchronized void waitForTask()
        {
            while (!this.done)
            {
                try
                {
                    this.wait();
                } catch (InterruptedException e)
                {
                }
            }
        }
    }

}
