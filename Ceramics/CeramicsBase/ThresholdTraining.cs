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
using System.Xml;

namespace Ceramics
{
    public partial class ThresholdTraining : Form
    {
        private const int ThumbWidth = 180, ThumbHeight = 135;
        public String xmlPath;
        public List<String>[] imagePaths;
        private ProbabilityMap probabilityMap;

        public ThresholdTraining()
        {
            InitializeComponent();
            imagePaths = new List<string>[2];
            imagePaths[0] = new List<string>();
            imagePaths[1] = new List<string>();
        }

        private void loadDataButton_Click(object sender, EventArgs e)
        {
            // Clear any existing thumbnails
            this.imageDataGrid.Rows.Clear();
            this.imagePaths[0].Clear();
            this.imagePaths[1].Clear();

            try
            {
                OpenFileDialog openFileDialog = new OpenFileDialog();
                openFileDialog.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
                openFileDialog.Filter = "XML Documents|*.xml|All Files|*.*";
                openFileDialog.FilterIndex = 0;

                if (openFileDialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
                {
                    this.xmlPath = System.IO.Path.GetDirectoryName(openFileDialog.FileName);
                    XmlDocument xmlDocument = new XmlDocument();
                    xmlDocument.Load(openFileDialog.FileName);

                    XmlNodeList nodeList = xmlDocument.SelectNodes("Training/Pair");

                    foreach (XmlNode node in nodeList)
                    {
                        XmlAttributeCollection attributes = node.Attributes;
                        LoadBitmapPaths(xmlPath + "\\" + attributes["source"].Value, xmlPath + "\\" + attributes["mask"].Value);
                    }
                }
            }
            catch
            {
                MessageBox.Show("Error loading training XML and associated images.");
                this.imageDataGrid.Rows.Clear();
                this.imagePaths[0].Clear();
                this.imagePaths[1].Clear();
                return;
            }
        }

        private void LoadBitmapPaths(String sourcePath, String maskPath)
        {
            imagePaths[0].Add(sourcePath);
            imagePaths[1].Add(maskPath);

            Bitmap thumb1 = new Bitmap(ThumbWidth, ThumbHeight, System.Drawing.Imaging.PixelFormat.Format24bppRgb);
            Bitmap thumb2 = new Bitmap(ThumbWidth, ThumbHeight, System.Drawing.Imaging.PixelFormat.Format24bppRgb);

            using (Graphics g = Graphics.FromImage(thumb1))
            {
                g.DrawImage(new Bitmap(sourcePath), new Rectangle(0, 0, ThumbWidth, ThumbHeight));
            }

            using (Graphics g = Graphics.FromImage(thumb2))
            {
                g.DrawImage(new Bitmap(maskPath), new Rectangle(0, 0, ThumbWidth, ThumbHeight));
            }

            this.imageDataGrid.Rows.Add(new object[] { thumb1, thumb2 });
        }

        private void createMapButton_Click(object sender, EventArgs e)
        {
            for (int i = 0; i < imagePaths[0].Count; i++)
            {
                using (Bitmap source = new Bitmap(imagePaths[0][i]))
                using (Bitmap mask = new Bitmap(imagePaths[1][i]))
                {
                    probabilityMap = new ProbabilityMap();
                    probabilityMap.TrainHistograms(source, mask);
                }
            }

            SaveFileDialog saveFileDialog = new SaveFileDialog();
            saveFileDialog.InitialDirectory = xmlPath;
            saveFileDialog.Filter = "Histogram Files|*.hst|All Files|*.*";
            saveFileDialog.FilterIndex = 0;
            MessageBox.Show("Training data created successfully. Please select a location to save to.");
            if (saveFileDialog.ShowDialog() == DialogResult.OK)
                this.probabilityMap.SaveHistograms(saveFileDialog.FileName);
            
            //if (this.probabilityMap != null)
                //this.probabilityMap.CreateMaps();

        }
    }
}
