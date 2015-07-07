package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Mat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class enables a given image to be processed concurrently over a number of threads.
 */
public class ThreadedGreyscaler extends Greyscaler
{

	/**
	 * This class takes a Greyscaler object and a sub region of an image (e.g.
	 * <code>Mat topHalf = mat.submat(0,mat.rows(),0,mat.cols()/2)</code>) so that the whole
	 * image can be processed over many threads. Greyscaler objects are not thread safe so
	 * don't use the same Greyscaler object in a different GreyscalerTask!
	 */
	protected static class GreyscalerTask implements Runnable
	{
		private Greyscaler greyscaler;
		private Mat rgbImage, greyscaleImage;

		private boolean done = false;

		public GreyscalerTask(Greyscaler greyscaler, Mat rgbImage, Mat greyscaleImage)
		{
			this.greyscaler = greyscaler;
			this.rgbImage = rgbImage;
			this.greyscaleImage = greyscaleImage;
		}

		public void run()
		{
			//greyscaler.justHueShiftImage(rgbImage, rgbImage);
			greyscaleImage = greyscaler.justGreyscaleImage(rgbImage, greyscaleImage);

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
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}

	private List<Greyscaler> greyscalers;
	private ExecutorService threadPool;

	/**
	 * As Greyscaler objects are not thread safe the number given in the List is also the
	 * maximum number of threads that can concurrently process an image.
	 *
	 * @param greyscalers
	 */
	public ThreadedGreyscaler(List<Greyscaler> greyscalers)
	{
		super();
		this.greyscalers = greyscalers;

		threadPool = Executors.newFixedThreadPool(this.greyscalers.size() - 1);
	}

	@Override
	public Mat greyscaleImage(Mat yuvImage, Mat greyscaleImage)
	{
//            int desiredRows = (yuvImage.rows() / 3) * 2, desiredCols = yuvImage.cols();
//            if (this.threeChannelBuffer == null || this.threeChannelBuffer.rows() != desiredRows || this.threeChannelBuffer.cols() != desiredCols)
//            {
//                Log.i(KEY, "Creating new Mat buffer (1)");
//                this.threeChannelBuffer = new Mat(desiredRows, desiredCols, CvType.CV_8UC3);
//            }
//            Imgproc.cvtColor(yuvImage, this.threeChannelBuffer, Imgproc.COLOR_YUV2BGR_NV21);
//
//            if (greyscaleImage == null || greyscaleImage.rows() != desiredRows || greyscaleImage.cols() != desiredCols)
//            {
//                greyscaleImage = new Mat(desiredRows, desiredCols, CvType.CV_8UC1);
//            }
//
//            List<GreyscalerTask> tasks = new ArrayList<>();
//            for (int i = 0; i < greyscalers.size(); ++i)
//            {
//                Greyscaler greyscaler = greyscalers.get(i);
//                GreyscalerTask task = new GreyscalerTask(
//                        greyscaler,
//                        this.threeChannelBuffer.submat(0, desiredRows, (desiredCols / greyscalers.size()) * i, (desiredCols / greyscalers.size()) * (i + 1)),
//                        greyscaleImage.submat(0, desiredRows, (desiredCols / greyscalers.size()) * i, (desiredCols / greyscalers.size()) * (i + 1)));
//                if (i < greyscalers.size() - 1)
//                {
//                    tasks.add(task);
//                    threadPool.execute(task);
//                } else
//                {
//                    task.run();
//                }
//            }
//            ;
//
//            for (GreyscalerTask task : tasks)
//            {
//                task.waitForTask();
//            }
//
//            if (this.invert)
//            {
//                Core.bitwise_not(greyscaleImage, greyscaleImage);
//            }
//
		return greyscaleImage;
	}

	@Override
	public void release()
	{
		super.release();
		threadPool.shutdown();
		for (Greyscaler greyscaler : greyscalers)
		{
			greyscaler.release();
		}
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
		return null;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return false;
	}
}