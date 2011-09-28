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
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;
using System.IO;

namespace CeramicsLauncher
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            // Write version?
            //Version V = new Version(1, 0, 0, 1000);
            //WriteVersion(V, "version");

            // Read Local Version
            Version localVersion = LocalVersionLookup();

            // Read Web Version
            Version remoteVersion = WebVersionLookup();

            if (remoteVersion != null
                && localVersion != null
                && remoteVersion.CompareTo(localVersion) > 0)
            {
                // Begin update process
                this.localVersionLabel.Text = localVersion.ToString();
                this.remoteVersionLabel.Text = remoteVersion.ToString();
            }
            else
            {
                // Launch ceramics process and end
                LaunchCeramics();
            }
        }

        private void LaunchCeramics()
        {
            // Launch the ceramics application and leave
            try
            {
                System.Diagnostics.Process.Start("ceramics.exe");
            }
            catch
            {
                MessageBox.Show("Ceramics application not found");
            }
            finally
            {
                Environment.Exit(0);
            }
        }

        public static void WriteVersion(Version V, String filename)
        {
            FileStream FS = new FileStream(filename, FileMode.OpenOrCreate, FileAccess.Write);
            System.Runtime.Serialization.Formatters.Binary.BinaryFormatter BF = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
            BF.Serialize(FS, V);
            FS.Close();
        }

        public static Version ReadVersion(Stream S)
        {
            System.Runtime.Serialization.Formatters.Binary.BinaryFormatter BF = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
            return BF.Deserialize(S) as Version;
        }

        public static Version LocalVersionLookup()
        {
            try
            {
                FileStream FS = new FileStream("version", FileMode.Open);
                Version localVersion = ReadVersion(FS);
                FS.Close();
                return localVersion;
            }
            catch
            {
                MessageBox.Show("Could not determine current system version.");
                return null;
            }
        }

        public static Version WebVersionLookup()
        {
            String remoteFilename = "http://www.cs.nott.ac.uk/~mpp/files/ceramics/version.serve";

            // Assign values to these objects here so that they can
            // be referenced in the finally block
            WebResponse response = null;

            // Use a try/catch/finally block as both the WebRequest and Stream
            // classes throw exceptions upon error
            try
            {
                // Create a request for the specified remote file name
                WebRequest request = WebRequest.Create(remoteFilename);
                if (request != null)
                {
                    // Send the request to the server and retrieve the
                    // WebResponse object 
                    response = request.GetResponse();
                    if (response != null)
                    {
                        System.Runtime.Serialization.Formatters.Binary.BinaryFormatter BF = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
                        return BF.Deserialize(response.GetResponseStream()) as Version;
                    }
                }
            }
            catch
            {
                MessageBox.Show("Could not determine server system version.");
                return null;
            }
            finally
            {
                // Close the response and streams objects here 
                // to make sure they're closed even if an exception
                // is thrown at some point
                if (response != null)
                {
                    response.Close();
                }
            }

            return null;
        }

        private void updateLaterButton_Click(object sender, EventArgs e)
        {
            LaunchCeramics();
        }

        private void updateButton_Click(object sender, EventArgs e)
        {
            // Alter relevant UI components
            this.label1.Text = "Updating...";
            this.label2.Text = "Current File: ";
            this.label3.Visible = false;
            this.localVersionLabel.Visible = false;
            this.remoteVersionLabel.Visible = false;
            this.updateButton.Visible = false;
            this.updateLaterButton.Visible = false;
            this.lineSeparator.Visible = false;
            this.updateProgressBar.Visible = true;
            this.updateInProgressButton.Visible = true;

            // Update
            String[] fileList = DownloadFileList();
            if (fileList == null)
            {
                MessageBox.Show("Error downloading updated file list. The update process will be terminated.");
                LaunchCeramics();
            }

            int i = 0;
            foreach (String fileName in fileList)
            {
                this.label2.Text = "Current File: " + fileName;
                bool success = DownloadFile(fileName);
                if (!success)
                {
                    MessageBox.Show("Error retrieving file: " + fileName + ". The update process will be terminated.");
                    LaunchCeramics();
                }
                this.updateProgressBar.Value = (int)((i+1) / (float)fileList.Length * 100);
                i++;
            }
            this.updateInProgressButton.Enabled = true;
            this.updateProgressBar.Value = 100;
        }

        private void updateInProgressButton_Click(object sender, EventArgs e)
        {
            LaunchCeramics();
        }

        public static String[] DownloadFileList()
        {

            String remoteFilename = "http://www.cs.nott.ac.uk/~mpp/files/ceramics/filelist.txt.serve";
            WebResponse response = null;
            List<String> LS = new List<string>();

            // Use a try/catch/finally block as both the WebRequest and Stream
            // classes throw exceptions upon error
            try
            {
                // Create a request for the specified remote file name
                WebRequest request = WebRequest.Create(remoteFilename);
                if (request != null)
                {
                    // Send the request to the server and retrieve the
                    // WebResponse object 
                    response = request.GetResponse();
                    if (response != null)
                    {
                        StreamReader SR = new StreamReader(response.GetResponseStream());
                        while (!SR.EndOfStream)
                        {
                            LS.Add(SR.ReadLine());
                        }
                    }
                }
            }
            catch
            {
                return null;
            }
            finally
            {
                if (response != null) response.Close();
            }

            return LS.ToArray();
        }

        public static bool DownloadFile(String fileName)
        {
            String URI = "http://www.cs.nott.ac.uk/~mpp/files/ceramics/";

            try
            {
                WebClient downloadClient = new WebClient();
                downloadClient.DownloadFile(URI + fileName + ".serve", fileName);
            }
            catch
            {
                MessageBox.Show("Error downloading file");
                return false;
            }
            return true;

        }

    }
}
