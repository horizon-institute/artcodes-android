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

using AForge.Video;
using AForge.Imaging;
using AForge;
using AForge.Video.DirectShow;
using AForge.Controls;

namespace Ceramics
{
    public partial class ChildWindow : Form
    {
        private int screenLink = 0;
        private MainForm parentMainForm;

        public ChildWindow(int i, int ScreenLink, MainForm ParentMainForm)
        {
            InitializeComponent();
            this.screenSize.SelectedIndex = 0;
            this.videoSourceSelection.SelectedIndex = i;
            this.screenLink = ScreenLink;
            this.parentMainForm = ParentMainForm;
        }

        public void SetSources(VideoCaptureDevice videoSource, VideoSourcePlayer.NewFrameHandler NFH, FormClosedEventHandler FCEH)
        {
            this.videoSourcePlayer.VideoSource = videoSource;
            this.videoSourcePlayer.NewFrame += NFH;
            this.FormClosed += FCEH;
        }

        private void screenSize_SelectedIndexChanged(object sender, EventArgs e)
        {
            Size newSize = new Size(512, 384);

            switch (screenSize.SelectedIndex)
            {
                default:
                    newSize = new Size(512, 384);
                    break;
                case 1:
                    newSize = new Size(768, 512);
                    break;
                case 2:
                    newSize = new Size(1024, 768);
                    break;
            }

            int xOffset = newSize.Width - this.videoSourcePlayer.Width;
            int yOffset = newSize.Height - this.videoSourcePlayer.Height;
            this.Width += xOffset;
            this.Height += yOffset;
            this.videoSourcePlayer.Size = newSize;
            this.screenSize.Left += xOffset;
            this.screenSizeLabel.Left += xOffset;
        }

        private void videoSourceSelection_SelectedIndexChanged(object sender, EventArgs e)
        {
            switch (this.screenLink)
            {
                case 1:
                    MainForm.SourceSelectDelegate SSD1 = new MainForm.SourceSelectDelegate(this.parentMainForm.source1Change);
                    this.parentMainForm.Invoke(SSD1, new object[] { this.videoSourceSelection.SelectedIndex });
                    break;
                case 2:
                    MainForm.SourceSelectDelegate SSD2 = new MainForm.SourceSelectDelegate(this.parentMainForm.source2Change);
                    this.parentMainForm.Invoke(SSD2, new object[] { this.videoSourceSelection.SelectedIndex });
                    break;
            }
        }
    }
}
