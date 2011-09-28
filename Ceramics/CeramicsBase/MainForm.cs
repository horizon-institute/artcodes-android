/*
    Copyright (c) 2011 University of Nottingham.
    Contact <richard.mortier@nottingham.ac.uk> for more info.
    Original code by Michael Pound <mpp@cs.nott.ac.uk>.

    This file is part of Ceramics.

    Ceramics is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Ceramics is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public
    License along with Ceramics.  If not, see
    <http://www.gnu.org/licenses/>.
*/
﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Xml;

using AForge.Video;
using AForge.Imaging;
using AForge;
using AForge.Video.DirectShow;
using AForge.Controls;
using AForge.Imaging.Filters;

namespace Ceramics
{
    public partial class MainForm : Form
    {
        private enum WorkerStatus
        {
            Busy, Ready, Finished, Aborted
        }

        private delegate void RunWorkerDelegate();
        public delegate void SourceSelectDelegate(int s);
        private delegate void ImageExporter(Bitmap bmp, SaveFileDialog openFileDialog);

        // list of video devices
        FilterInfoCollection videoDevices;

        // size
        private const int DesiredWidth = 640;
        private const int DesiredHeight = 480;

        // blob finder
        ComponentFinder componentFinder;

        // rag constructor
        RegionAdjacencyGraph regionAdjacencyGraph;

        // Marker Detector
        MarkerDetector markerDetector;

        // Code rules
        List<MarkerLabel> detectedMarkerList = null;
        List<MarkerLabel> permittedMarkerList = null;
        List<string> permittedCodes = new List<string>();

        // filters
        AForge.Imaging.Filters.Grayscale grayFilter;
        AForge.Imaging.Filters.OtsuThreshold otsuFilter;     
 
        // image buffers
        Bitmap grayBuffer;
        Bitmap thresholdedImage = null;
        Bitmap processingImage = null;
        Bitmap processedImage = null;
      
        System.Threading.ReaderWriterLockSlim readerWriterLockThresholdedImage = new ReaderWriterLockSlim();
        System.Threading.ReaderWriterLockSlim readerWriterLockProcessedImage = new ReaderWriterLockSlim();
        ManualResetEvent resetEvent = null;
        WorkerStatus workerStatus = WorkerStatus.Ready;
        float workerTicks;

        // Child window declarations
        ChildWindow CWViewer1;
        ChildWindow CWViewer2;
        VideoCaptureDevice videoCaptureDevice;
        volatile int screen1Switch = 0;
        volatile int screen2Switch = 0;

        // Image Capture
        bool capture1 = false;
        bool capture2 = false;
        ImageExporter imageExporter;
        SaveFileDialog openFileDialog;

        // Thresholdiong
        ProbabilityMap probabilityMap;

        // XML output
        XmlDocument xmlDocument;

        public MainForm( )
        {
            InitializeComponent( );
            cameraFpsLabel.Text = string.Empty;
            permittedCodesLabel.Text = string.Empty;

            // initialise filters
            grayFilter = new AForge.Imaging.Filters.Grayscale(0.2125, 0.7154, 0.0721);
            otsuFilter = new AForge.Imaging.Filters.OtsuThreshold();

            // image buffers
            grayBuffer = new Bitmap(DesiredWidth, DesiredHeight, System.Drawing.Imaging.PixelFormat.Format8bppIndexed);

            // component finder
            componentFinder = new ComponentFinder();

            // rag constructor
            regionAdjacencyGraph = new RegionAdjacencyGraph(DesiredWidth, DesiredHeight);

            // Marker Detector
            markerDetector = new MarkerDetector();

            // Worker
            resetEvent = new ManualResetEvent(false);

            // Image exporter
            imageExporter = new ImageExporter(this.ExportImage);
            openFileDialog = new SaveFileDialog();
            openFileDialog.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
            openFileDialog.Filter = "Bitmap Images|*.bmp|JPEG Images|*.jpg|PNG Images|*.png|All Files|*.*";

            // Probability maps
            probabilityMap = null;


            xmlDocument = new XmlDocument();
            xmlDocument.LoadXml("<!DOCTYPE CERAMICS [<!ELEMENT STREAMS (STREAM+)><!ELEMENT STREAM (MARKER+)><!ELEMENT MARKER (EMPTY)><!ATTLIST STREAM DATE CDATA #REQUIRED><!ATTLIST STREAM ID CDATA #REQUIRED><!ATTLIST MARKER CODE CDATA #REQUIRED><!ATTLIST MARKER TIMESTAMP CDATA #REQUIRED><!ATTLIST MARKER X1 CDATA #REQUIRED><!ATTLIST MARKER Y1 CDATA #REQUIRED><!ATTLIST MARKER X2 CDATA #REQUIRED><!ATTLIST MARKER Y2 CDATA #REQUIRED>]><stream></stream>");

            XmlElement root = xmlDocument.DocumentElement;
            root.SetAttribute("date",DateTime.Now.Date.ToString().Substring(0,10));
            root.SetAttribute("id", "0001");

            Console.WriteLine(xmlDocument.OuterXml);

            xmlDocument.Save(Environment.GetFolderPath(Environment.SpecialFolder.Desktop) + "\\dtd.xml");




            // show device list
			try
			{
                // enumerate video devices
                videoDevices = new FilterInfoCollection( FilterCategory.VideoInputDevice );

                if ( videoDevices.Count == 0 )
                {
                    throw new Exception( );
                }

                for ( int i = 1, n = videoDevices.Count; i <= n; i++ )
                {
                    string cameraName = i + " : " + videoDevices[i - 1].Name;

                    cameraCombo.Items.Add( cameraName );
                }

                cameraCombo.SelectedIndex = 0;
            }
            catch
            {
                startButton.Enabled = false;

                cameraCombo.Items.Add( "No cameras found" );

                cameraCombo.SelectedIndex = 0;

                cameraCombo.Enabled = false;
            }

            this.videoSource1Selection.SelectedIndex = 0;
            this.videoSource2Selection.SelectedIndex = 2;
        }

        // On form closing
        private void MainForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            StopCameras( );
        }

        // On "Start" button click
        private void startButton_Click( object sender, EventArgs e )
        {
            StartCameras( );

            startButton.Enabled = false;
            stopButton.Enabled = true;

            addCodesButton.Enabled = true;

            screenViewer1Button.Enabled = true;
            screenViewer2Button.Enabled = true;

            captureScreen1.Enabled = true;
            captureScreen2.Enabled = true;

            UpdatePermittedCodesUI();
        }

        // On "Stop" button click
        private void stopButton_Click( object sender, EventArgs e )
        {
            StopCameras( );

            startButton.Enabled = true;
            stopButton.Enabled = false;

            addCodesButton.Enabled = false;

            screenViewer1Button.Enabled = false;
            screenViewer2Button.Enabled = false;

            cameraFpsLabel.Text = string.Empty;
            permittedCodesLabel.Text = string.Empty;

            captureScreen1.Enabled = false;
            captureScreen2.Enabled = false;

            // Output XML data of the stream
            
            SaveFileDialog svfd = new SaveFileDialog();
            svfd.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";
            svfd.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            if (svfd.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                xmlDocument.Save(svfd.FileName);
            }
        }

        // Start cameras
        private void StartCameras( )
        {
            // create first video source
            videoCaptureDevice = new VideoCaptureDevice( videoDevices[cameraCombo.SelectedIndex].MonikerString );

            videoCaptureDevice.DesiredFrameSize = new Size(DesiredWidth, DesiredHeight);
            videoCaptureDevice.DesiredFrameRate = 25;
           
            videoSourcePlayer1.VideoSource = videoCaptureDevice;

            if (CWViewer1 == null || !CWViewer1.Visible)
                videoSourcePlayer1.VideoSource = videoCaptureDevice;

            if (CWViewer2 == null || !CWViewer2.Visible)
                videoSourcePlayer2.VideoSource = videoCaptureDevice;
            
            
            // Start feed
            videoCaptureDevice.NewFrame += new NewFrameEventHandler(videoSource_NewFrame);
            videoCaptureDevice.Start();

            // Start image processing worker
            new RunWorkerDelegate(this.RunWorker).BeginInvoke(null, this);

            // start timer
            timer.Start( );
        }

        unsafe void videoSource_NewFrame(object sender, NewFrameEventArgs eArgs)
        {
            Bitmap image = eArgs.Frame;
            try
            {
                if (this.workerStatus == WorkerStatus.Finished)
                {
                    if (processedImage != null)
                    {
                        using (Graphics g = Graphics.FromImage(processedImage))
                        {
                            g.DrawImageUnscaled(processingImage, 0, 0);
                        }
                    }
                    else
                    {
                        processedImage = new Bitmap(processingImage);
                    }

                    if (thresholdedImage != null)
                    {
                        using (Graphics g = Graphics.FromImage(thresholdedImage))
                        {
                            g.DrawImageUnscaled(grayBuffer, 0, 0);
                        }
                    }
                    else
                    {
                        thresholdedImage = new Bitmap(grayBuffer);
                    }

                    if (processingImage != null)
                    {
                        using (Graphics g = Graphics.FromImage(processingImage))
                        {
                            g.DrawImageUnscaled(image, 0, 0);
                        }
                    }
                    else
                    {
                        processingImage = new Bitmap(image);
                    }
                    resetEvent.Set();
                }
                else if (this.workerStatus == WorkerStatus.Ready)
                {
                    if (processingImage != null)
                    {
                        using (Graphics g = Graphics.FromImage(processingImage))
                        {
                            g.DrawImageUnscaled(image, 0, 0);
                        }
                    }
                    else
                    {
                        processingImage = new Bitmap(image);

                    }
                    resetEvent.Set();
                }
            }
            catch (Exception e)
            {
                MessageBox.Show("Exception occurred while handling new frame: " + e.Message);
                throw;
            }
        }

        unsafe void videoSourcePlayer1_NewFrame(object sender, ref Bitmap image)
        {
            switch (screen1Switch)
            {
                case 1:
                    // Copy thresholded image
                    readerWriterLockThresholdedImage.EnterReadLock();
                    try
                    {
                        if (thresholdedImage != null)
                        {
                            using (Graphics g = Graphics.FromImage(image))
                            {
                                g.DrawImageUnscaled(thresholdedImage, 0, 0);
                            }
                        }
                    }
                    finally
                    {
                        readerWriterLockThresholdedImage.ExitReadLock();
                    }
                    break;
                case 2:
                    // Copy marker image
                    readerWriterLockProcessedImage.EnterReadLock();
                    try
                    {
                        if (processedImage != null)
                        {
                            using (Graphics g = Graphics.FromImage(image))
                            {
                                g.DrawImageUnscaled(processedImage, 0, 0);
                            }
                        }
                    }
                    finally
                    {
                        readerWriterLockProcessedImage.ExitReadLock();
                    }
                    break;
                default:
                    // Do not alter image
                    break;
            }
            if (capture1)
            {
                //imageExporter.BeginInvoke(new Bitmap(image), this.openFileDialog, null, this);
                this.BeginInvoke(this.imageExporter, new object[] { new Bitmap(image), this.openFileDialog });
                capture1 = false;
            }
        }

        private void videoSourcePlayer2_NewFrame(object sender, ref Bitmap image)
        {
            switch (screen2Switch)
            {
                case 1:
                    // Copy thresholded image
                    readerWriterLockThresholdedImage.EnterReadLock();
                    try
                    {
                        if (thresholdedImage != null)
                        {
                            using (Graphics g = Graphics.FromImage(image))
                            {
                                g.DrawImageUnscaled(thresholdedImage, 0, 0);
                            }
                        }
                    }
                    finally
                    {
                        readerWriterLockThresholdedImage.ExitReadLock();
                    }
                    break;
                case 2:
                    // Copy marker image
                    readerWriterLockProcessedImage.EnterReadLock();
                    try
                    {
                        if (processedImage != null)
                        {
                            using (Graphics g = Graphics.FromImage(image))
                            {
                                g.DrawImageUnscaled(processedImage, 0, 0);
                            }
                        }
                    }
                    finally
                    {
                        readerWriterLockProcessedImage.ExitReadLock();
                    }
                    break;
                default:
                    // Do not alter image
                    break;
            }

            if (capture2)
            {
                //imageExporter.BeginInvoke(new Bitmap(image), this.openFileDialog, null, this);
                this.BeginInvoke(this.imageExporter, new object[] { new Bitmap(image), this.openFileDialog });
                capture2 = false;
            }
        }

        public void RunWorker()
        {
            while (true)
            {
                resetEvent.WaitOne();

                // Process image
                this.workerStatus = WorkerStatus.Busy;

                if (processingImage.Width != grayBuffer.Width || processingImage.Height != grayBuffer.Height)
                    grayBuffer = new Bitmap(processingImage.Width, processingImage.Height, System.Drawing.Imaging.PixelFormat.Format8bppIndexed);

                System.Drawing.Imaging.BitmapData imageBD = LockTotalBitmap(processingImage, System.Drawing.Imaging.ImageLockMode.ReadWrite);
                System.Drawing.Imaging.BitmapData grayBufferBD = LockTotalBitmap(grayBuffer, System.Drawing.Imaging.ImageLockMode.ReadWrite);

                try
                {
                    // create unmanaged images
                    UnmanagedImage imageUMI = new UnmanagedImage(imageBD);
                    UnmanagedImage grayBufferUMI = new UnmanagedImage(grayBufferBD);

                    // Apply grayscale and otsu filters
                    if (this.probabilityMap == null)
                    {
                        // Grayscale Otsu Thresholding
                        grayFilter.Apply(imageUMI, grayBufferUMI);
                        
                      
                        otsuFilter.ApplyInPlace(grayBufferUMI);
                        //Median mf = new Median(5);
                        //mf.ApplyInPlace(grayBufferUMI);
                        
                    }
                    else
                    {
                        // BPM Colour Thresholding
                        probabilityMap.TresholdImage(imageUMI, grayBufferUMI);

                        //Median mf = new Median(5);
                        //mf.ApplyInPlace(grayBufferUMI);

                        GaussianBlur gb = new GaussianBlur(3.0);
                        Threshold t = new Threshold(127);
                        gb.ApplyInPlace(grayBufferUMI);
                        t.ApplyInPlace(grayBufferUMI);

                        //AForge.Imaging.Filters.Erosion er = new AForge.Imaging.Filters.Erosion();
                        //er.ApplyInPlace(grayBufferUMI);
                    }

                    bool ccSuccess = componentFinder.FindBlobs(grayBufferUMI);

                    // Create adjacency matrix
                    bool ajSuccess = false;
                    if (ccSuccess)
                    {
                        ajSuccess = regionAdjacencyGraph.ConstructGraph(componentFinder.ObjectLabels, componentFinder.ObjectCount, processingImage.Width, processingImage.Height);
                    }
                    
                    // If producing the graph has failed, for example due to too many regions, stop processing the current frame.
                    if (!ajSuccess)
                    {
                        if (detectedMarkerList != null)
                            detectedMarkerList.Clear();
                        if (permittedMarkerList != null)
                            permittedMarkerList.Clear();
                        this.workerStatus = WorkerStatus.Finished;
                        resetEvent.Reset();
                        continue;
                    }

                    detectedMarkerList = markerDetector.FindMarkers(componentFinder, regionAdjacencyGraph);
                    permittedMarkerList = markerDetector.PermitMarkers(detectedMarkerList, permittedCodes);


                    // Add markers to XML document
                    /*
                    XmlNode newElem;
                    newElem = doc.CreateNode(XmlNodeType.Element, "pages", "");
                    newElem.InnerText = "290";

                    Console.WriteLine("Add the new element to the document...");
                    root = doc.DocumentElement;
                    root.AppendChild(newElem);
                    */

                    string TimeStamp = DateTime.Now.TimeOfDay.ToString().Substring(0,12);
                    foreach (MarkerLabel ml in permittedMarkerList)
                    {
                        BoundingBox bb = componentFinder.ObjectBoundingBoxes[ml.ID];
                        XmlNode newElement;
                        newElement = xmlDocument.CreateNode(XmlNodeType.Element, "marker", "");

                        // Attributes
                        XmlAttribute x1 = xmlDocument.CreateAttribute("x1");
                        x1.InnerText = bb.x1.ToString();
                        XmlAttribute x2 = xmlDocument.CreateAttribute("x2");
                        x2.InnerText = bb.x2.ToString();
                        XmlAttribute y1 = xmlDocument.CreateAttribute("y1");
                        y1.InnerText = bb.y1.ToString();
                        XmlAttribute y2 = xmlDocument.CreateAttribute("y2");
                        y2.InnerText = bb.y2.ToString();

                        XmlAttribute code = xmlDocument.CreateAttribute("code");
                        code.InnerText = ml.Code;

                        XmlAttribute timestamp = xmlDocument.CreateAttribute("timestamp");
                        timestamp.InnerText = TimeStamp;

                        newElement.Attributes.Append(timestamp);
                        newElement.Attributes.Append(code);
                        newElement.Attributes.Append(x1);
                        newElement.Attributes.Append(y1);
                        newElement.Attributes.Append(x2);
                        newElement.Attributes.Append(y2);

                        xmlDocument.DocumentElement.AppendChild(newElement);
                    }
                }
                catch (Exception e)
                {
                    MessageBox.Show("An exception occurred during Marker Detection: " + e.Message);
                    throw;
                }
                finally
                {
                    processingImage.UnlockBits(imageBD);
                    grayBuffer.UnlockBits(grayBufferBD);
                }

                // Draw other UI components
                if (detectedMarkerList != null && permittedMarkerList.Count > 0)
                {
                    using (Graphics g = Graphics.FromImage(processingImage))
                    {
                        Pen boundingBoxPen = new Pen(Color.Blue, 4.0f);
                        boundingBoxPen.StartCap = System.Drawing.Drawing2D.LineCap.Round;
                        boundingBoxPen.EndCap = System.Drawing.Drawing2D.LineCap.Round;
                        Font markerFont = new Font("Arial", 16.0f);

                        for (int marker = 0; marker < permittedMarkerList.Count; marker++)
                        {
                            int markerID = permittedMarkerList[marker].ID;
                            string markerCode = permittedMarkerList[marker].Code;
                            BoundingBox bb = componentFinder.ObjectBoundingBoxes[markerID];

                            g.DrawLine(boundingBoxPen, new Point(bb.x1, bb.y1), new Point(bb.x1, bb.y2));
                            g.DrawLine(boundingBoxPen, new Point(bb.x1, bb.y2), new Point(bb.x2, bb.y2));
                            g.DrawLine(boundingBoxPen, new Point(bb.x2, bb.y2), new Point(bb.x2, bb.y1));
                            g.DrawLine(boundingBoxPen, new Point(bb.x2, bb.y1), new Point(bb.x1, bb.y1));
                        }

                        for (int marker = 0; marker < permittedMarkerList.Count; marker++)
                        {
                            int markerID = permittedMarkerList[marker].ID;
                            string markerCode = permittedMarkerList[marker].Code;
                            BoundingBox bb = componentFinder.ObjectBoundingBoxes[markerID];
                            g.DrawString(markerCode, markerFont, Brushes.Red, new PointF(bb.x1, bb.y2));
                        }
                    }
                }

                this.workerStatus = WorkerStatus.Finished;
                workerTicks++;
                resetEvent.Reset();
            }
        }

        System.Drawing.Imaging.BitmapData LockTotalBitmap(Bitmap image, System.Drawing.Imaging.ImageLockMode ILM)
        {
            return image.LockBits(new Rectangle(0, 0, image.Width, image.Height), ILM, image.PixelFormat);
        }

        // Stop cameras
        private void StopCameras( )
        {
            timer.Stop( );

            if (videoCaptureDevice != null)
            {
                videoCaptureDevice.SignalToStop();
                videoCaptureDevice.WaitForStop();
            }
            
            if (CWViewer1 != null)
                CWViewer1.Close();
            if (CWViewer2 != null)
                CWViewer2.Close();
        }

        // On timer tick - collect statistics
        private void timer_Tick( object sender, EventArgs e )
        {
            float fps1 = workerTicks;
            workerTicks = 0;
            if (componentFinder.ObjectCount > 1500)
                cameraFpsLabel.Text = "1500+ regions, " + fps1.ToString() + " fps";
            else
                cameraFpsLabel.Text = componentFinder.ObjectCount.ToString() + " regions, " + fps1.ToString() + " fps";
        }

        private void UpdatePermittedCodesUI()
        {
            if (permittedCodes == null || permittedCodes.Count == 0)
            {
                permittedCodesLabel.Text = "All codes permitted.";
                addCodesButton.Enabled = true;
                removeCodesButton.Enabled = false;
                clearCodesButton.Enabled = false;
            }
            else
            {
                string permittedCodesString = "Permitted Codes: ";
                foreach (string code in permittedCodes)
                {
                    permittedCodesString += code + " ";
                }
                permittedCodesLabel.Text = permittedCodesString;
                addCodesButton.Enabled = true;
                removeCodesButton.Enabled = true;
                clearCodesButton.Enabled = true;
            }
        }

        private void addCodesButton_Click(object sender, EventArgs e)
        {
            foreach (MarkerLabel ml in detectedMarkerList)
            {
                string currentCode = ml.Code;
                if (permittedCodes.IndexOf(currentCode) < 0)
                {
                    permittedCodes.Add(currentCode);
                }
            }
            UpdatePermittedCodesUI();
        }

        private void removeCodesButton_Click(object sender, EventArgs e)
        {
            foreach (MarkerLabel ml in detectedMarkerList)
            {
                string currentCode = ml.Code;
                if (permittedCodes.IndexOf(currentCode) >= 0)
                {
                    permittedCodes.RemoveAt(permittedCodes.IndexOf(currentCode));
                }
            }
            UpdatePermittedCodesUI();
        }

        private void clearCodesButton_Click(object sender, EventArgs e)
        {
            permittedCodes.Clear();
            UpdatePermittedCodesUI();
        }

        private void screenViewer1Button_Click(object sender, EventArgs e)
        {
            CWViewer1 = new ChildWindow(this.videoSource1Selection.SelectedIndex, 1, this);
            CWViewer1.SetSources((VideoCaptureDevice)videoSourcePlayer1.VideoSource,
                new VideoSourcePlayer.NewFrameHandler(this.videoSourcePlayer1_NewFrame),
                new FormClosedEventHandler(ChildWindow1_FormClosed));
            this.videoSourcePlayer1.VideoSource = null;
            CWViewer1.Show(this);
        }

        private void ChildWindow1_FormClosed(object sender, FormClosedEventArgs e)
        {
            this.videoSourcePlayer1.VideoSource = videoCaptureDevice;
            CWViewer1.Dispose();
            CWViewer1 = null;
        }

        private void screenViewer2Button_Click(object sender, EventArgs e)
        {
            CWViewer2 = new ChildWindow(this.videoSource2Selection.SelectedIndex, 2, this);
            CWViewer2.SetSources((VideoCaptureDevice)videoSourcePlayer2.VideoSource,
                new VideoSourcePlayer.NewFrameHandler(this.videoSourcePlayer2_NewFrame),
                new FormClosedEventHandler(ChildWindow2_FormClosed));
            this.videoSourcePlayer2.VideoSource = null;
            CWViewer2.Show(this);
        }

        private void ChildWindow2_FormClosed(object sender, FormClosedEventArgs e)
        {
            this.videoSourcePlayer2.VideoSource = videoCaptureDevice;
            CWViewer2.Dispose();
            CWViewer2 = null;
        }

        private void videoSource1Selection_SelectedIndexChanged(object sender, EventArgs e)
        {
            screen1Switch = videoSource1Selection.SelectedIndex;
        }

        private void videoSource2Selection_SelectedIndexChanged(object sender, EventArgs e)
        {
            screen2Switch = videoSource2Selection.SelectedIndex;
        }

        public void source1Change(int s)
        {
            this.videoSource1Selection.SelectedIndex = s;
        }

        public void source2Change(int s)
        {
            this.videoSource2Selection.SelectedIndex = s;
        }

        private void captureScreen1_Click(object sender, EventArgs e)
        {
            this.capture1 = true;
        }

        private void captureScreen2_Click(object sender, EventArgs e)
        {
            this.capture2 = true;
        }

        private void ExportImage(Bitmap bmp, SaveFileDialog saveFileDialog)
        {
            if (saveFileDialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                bmp.Save(saveFileDialog.FileName);
            }
            bmp.Dispose();
        }

        private void thresholdCreateLabel_Click(object sender, EventArgs e)
        {
            ThresholdTraining thresholdTraining = new ThresholdTraining();
            thresholdTraining.ShowDialog();
        }

        private void thresholdOpenLabel_Click(object sender, EventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            openFileDialog.Filter = "Histograms|*.hst|All Files|*.*";
            openFileDialog.FilterIndex = 0;

            if (openFileDialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                // Initialise Probability Map
                this.probabilityMap = new ProbabilityMap(openFileDialog.FileName);

                // Convert histograms into probability maps
                this.probabilityMap.CreateMaps();
            }
        }
    }
}
