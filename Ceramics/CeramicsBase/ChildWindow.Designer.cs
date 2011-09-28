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
﻿namespace Ceramics
{
    partial class ChildWindow
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.videoSourcePlayer = new AForge.Controls.VideoSourcePlayer();
            this.screenSize = new System.Windows.Forms.ComboBox();
            this.screenSizeLabel = new System.Windows.Forms.Label();
            this.videoSourceSelection = new System.Windows.Forms.ComboBox();
            this.screen1Label = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // videoSourcePlayer
            // 
            this.videoSourcePlayer.BackColor = System.Drawing.SystemColors.ControlDark;
            this.videoSourcePlayer.ForeColor = System.Drawing.Color.White;
            this.videoSourcePlayer.Location = new System.Drawing.Point(12, 39);
            this.videoSourcePlayer.Name = "videoSourcePlayer";
            this.videoSourcePlayer.Size = new System.Drawing.Size(512, 384);
            this.videoSourcePlayer.TabIndex = 1;
            this.videoSourcePlayer.VideoSource = null;
            // 
            // screenSize
            // 
            this.screenSize.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.screenSize.FormattingEnabled = true;
            this.screenSize.Items.AddRange(new object[] {
            "Small",
            "Medium",
            "Large"});
            this.screenSize.Location = new System.Drawing.Point(388, 12);
            this.screenSize.Name = "screenSize";
            this.screenSize.Size = new System.Drawing.Size(136, 21);
            this.screenSize.TabIndex = 12;
            this.screenSize.SelectedIndexChanged += new System.EventHandler(this.screenSize_SelectedIndexChanged);
            // 
            // screenSizeLabel
            // 
            this.screenSizeLabel.Location = new System.Drawing.Point(301, 15);
            this.screenSizeLabel.Name = "screenSizeLabel";
            this.screenSizeLabel.Size = new System.Drawing.Size(81, 14);
            this.screenSizeLabel.TabIndex = 11;
            this.screenSizeLabel.Text = "Screen Size: ";
            // 
            // videoSourceSelection
            // 
            this.videoSourceSelection.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.videoSourceSelection.FormattingEnabled = true;
            this.videoSourceSelection.Items.AddRange(new object[] {
            "Source Video",
            "Thresholded Video",
            "Marker Detection"});
            this.videoSourceSelection.Location = new System.Drawing.Point(99, 12);
            this.videoSourceSelection.Name = "videoSourceSelection";
            this.videoSourceSelection.Size = new System.Drawing.Size(136, 21);
            this.videoSourceSelection.TabIndex = 14;
            this.videoSourceSelection.SelectedIndexChanged += new System.EventHandler(this.videoSourceSelection_SelectedIndexChanged);
            // 
            // screen1Label
            // 
            this.screen1Label.Location = new System.Drawing.Point(12, 15);
            this.screen1Label.Name = "screen1Label";
            this.screen1Label.Size = new System.Drawing.Size(81, 18);
            this.screen1Label.TabIndex = 13;
            this.screen1Label.Text = "Source: ";
            // 
            // ChildWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(536, 435);
            this.Controls.Add(this.videoSourceSelection);
            this.Controls.Add(this.screen1Label);
            this.Controls.Add(this.screenSize);
            this.Controls.Add(this.screenSizeLabel);
            this.Controls.Add(this.videoSourcePlayer);
            this.Name = "ChildWindow";
            this.Text = "Screen Viewer";
            this.ResumeLayout(false);

        }

        #endregion

        private AForge.Controls.VideoSourcePlayer videoSourcePlayer;
        private System.Windows.Forms.ComboBox screenSize;
        private System.Windows.Forms.Label screenSizeLabel;
        private System.Windows.Forms.ComboBox videoSourceSelection;
        private System.Windows.Forms.Label screen1Label;
    }
}