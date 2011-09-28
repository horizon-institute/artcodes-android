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
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose( bool disposing )
        {
            if ( disposing && ( components != null ) )
            {
                components.Dispose( );
            }
            base.Dispose( disposing );
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent( )
        {
            this.components = new System.ComponentModel.Container();
            this.startButton = new System.Windows.Forms.Button();
            this.stopButton = new System.Windows.Forms.Button();
            this.timer = new System.Windows.Forms.Timer(this.components);
            this.addCodesButton = new System.Windows.Forms.Button();
            this.clearCodesButton = new System.Windows.Forms.Button();
            this.removeCodesButton = new System.Windows.Forms.Button();
            this.cameraCombo = new System.Windows.Forms.ComboBox();
            this.videoSourcePlayer1 = new AForge.Controls.VideoSourcePlayer();
            this.videoSourcePlayer2 = new AForge.Controls.VideoSourcePlayer();
            this.cameraFpsLabel = new System.Windows.Forms.Label();
            this.permittedCodesLabel = new System.Windows.Forms.Label();
            this.trainingGroupBox = new System.Windows.Forms.GroupBox();
            this.screenGroupBox = new System.Windows.Forms.GroupBox();
            this.videoSource2Selection = new System.Windows.Forms.ComboBox();
            this.videoSource1Selection = new System.Windows.Forms.ComboBox();
            this.screen2Label = new System.Windows.Forms.Label();
            this.screen1Label = new System.Windows.Forms.Label();
            this.screenViewer2Button = new System.Windows.Forms.Button();
            this.screenViewer1Button = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.thresholdMethodLabel = new System.Windows.Forms.Label();
            this.thresholdCreateLabel = new System.Windows.Forms.Button();
            this.thresholdOpenLabel = new System.Windows.Forms.Button();
            this.trainingFileLabel = new System.Windows.Forms.Label();
            this.captureScreen1 = new System.Windows.Forms.Button();
            this.captureScreen2 = new System.Windows.Forms.Button();
            this.trainingGroupBox.SuspendLayout();
            this.screenGroupBox.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.SuspendLayout();
            // 
            // startButton
            // 
            this.startButton.Location = new System.Drawing.Point(791, 446);
            this.startButton.Name = "startButton";
            this.startButton.Size = new System.Drawing.Size(122, 77);
            this.startButton.TabIndex = 4;
            this.startButton.Text = "&Start";
            this.startButton.UseVisualStyleBackColor = true;
            this.startButton.Click += new System.EventHandler(this.startButton_Click);
            // 
            // stopButton
            // 
            this.stopButton.Enabled = false;
            this.stopButton.Location = new System.Drawing.Point(919, 446);
            this.stopButton.Name = "stopButton";
            this.stopButton.Size = new System.Drawing.Size(122, 77);
            this.stopButton.TabIndex = 5;
            this.stopButton.Text = "S&top";
            this.stopButton.UseVisualStyleBackColor = true;
            this.stopButton.Click += new System.EventHandler(this.stopButton_Click);
            // 
            // timer
            // 
            this.timer.Interval = 1000;
            this.timer.Tick += new System.EventHandler(this.timer_Tick);
            // 
            // addCodesButton
            // 
            this.addCodesButton.Enabled = false;
            this.addCodesButton.Location = new System.Drawing.Point(6, 19);
            this.addCodesButton.Name = "addCodesButton";
            this.addCodesButton.Size = new System.Drawing.Size(136, 23);
            this.addCodesButton.TabIndex = 6;
            this.addCodesButton.Text = "Add Detector Codes";
            this.addCodesButton.UseVisualStyleBackColor = true;
            this.addCodesButton.Click += new System.EventHandler(this.addCodesButton_Click);
            // 
            // clearCodesButton
            // 
            this.clearCodesButton.Enabled = false;
            this.clearCodesButton.Location = new System.Drawing.Point(147, 19);
            this.clearCodesButton.Name = "clearCodesButton";
            this.clearCodesButton.Size = new System.Drawing.Size(80, 52);
            this.clearCodesButton.TabIndex = 7;
            this.clearCodesButton.Text = "Clear Training";
            this.clearCodesButton.UseVisualStyleBackColor = true;
            this.clearCodesButton.Click += new System.EventHandler(this.clearCodesButton_Click);
            // 
            // removeCodesButton
            // 
            this.removeCodesButton.Enabled = false;
            this.removeCodesButton.Location = new System.Drawing.Point(6, 48);
            this.removeCodesButton.Name = "removeCodesButton";
            this.removeCodesButton.Size = new System.Drawing.Size(136, 23);
            this.removeCodesButton.TabIndex = 8;
            this.removeCodesButton.Text = "Remove Detector Codes";
            this.removeCodesButton.UseVisualStyleBackColor = true;
            this.removeCodesButton.Click += new System.EventHandler(this.removeCodesButton_Click);
            // 
            // cameraCombo
            // 
            this.cameraCombo.FormattingEnabled = true;
            this.cameraCombo.Location = new System.Drawing.Point(12, 12);
            this.cameraCombo.Name = "cameraCombo";
            this.cameraCombo.Size = new System.Drawing.Size(326, 21);
            this.cameraCombo.TabIndex = 3;
            // 
            // videoSourcePlayer1
            // 
            this.videoSourcePlayer1.BackColor = System.Drawing.SystemColors.ControlDark;
            this.videoSourcePlayer1.ForeColor = System.Drawing.Color.White;
            this.videoSourcePlayer1.Location = new System.Drawing.Point(12, 39);
            this.videoSourcePlayer1.Name = "videoSourcePlayer1";
            this.videoSourcePlayer1.Size = new System.Drawing.Size(512, 384);
            this.videoSourcePlayer1.TabIndex = 5;
            this.videoSourcePlayer1.VideoSource = null;
            this.videoSourcePlayer1.NewFrame += new AForge.Controls.VideoSourcePlayer.NewFrameHandler(this.videoSourcePlayer1_NewFrame);
            // 
            // videoSourcePlayer2
            // 
            this.videoSourcePlayer2.BackColor = System.Drawing.SystemColors.ControlDark;
            this.videoSourcePlayer2.ForeColor = System.Drawing.Color.White;
            this.videoSourcePlayer2.Location = new System.Drawing.Point(530, 39);
            this.videoSourcePlayer2.Name = "videoSourcePlayer2";
            this.videoSourcePlayer2.Size = new System.Drawing.Size(512, 384);
            this.videoSourcePlayer2.TabIndex = 0;
            this.videoSourcePlayer2.VideoSource = null;
            this.videoSourcePlayer2.NewFrame += new AForge.Controls.VideoSourcePlayer.NewFrameHandler(this.videoSourcePlayer2_NewFrame);
            // 
            // cameraFpsLabel
            // 
            this.cameraFpsLabel.Location = new System.Drawing.Point(814, 426);
            this.cameraFpsLabel.Name = "cameraFpsLabel";
            this.cameraFpsLabel.Size = new System.Drawing.Size(228, 17);
            this.cameraFpsLabel.TabIndex = 4;
            this.cameraFpsLabel.Text = "label1";
            this.cameraFpsLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // permittedCodesLabel
            // 
            this.permittedCodesLabel.Location = new System.Drawing.Point(12, 426);
            this.permittedCodesLabel.Name = "permittedCodesLabel";
            this.permittedCodesLabel.Size = new System.Drawing.Size(408, 17);
            this.permittedCodesLabel.TabIndex = 7;
            this.permittedCodesLabel.Text = "label3";
            // 
            // trainingGroupBox
            // 
            this.trainingGroupBox.Controls.Add(this.addCodesButton);
            this.trainingGroupBox.Controls.Add(this.removeCodesButton);
            this.trainingGroupBox.Controls.Add(this.clearCodesButton);
            this.trainingGroupBox.Location = new System.Drawing.Point(12, 446);
            this.trainingGroupBox.Name = "trainingGroupBox";
            this.trainingGroupBox.Size = new System.Drawing.Size(233, 77);
            this.trainingGroupBox.TabIndex = 9;
            this.trainingGroupBox.TabStop = false;
            this.trainingGroupBox.Text = "Detector Training";
            // 
            // screenGroupBox
            // 
            this.screenGroupBox.Controls.Add(this.videoSource2Selection);
            this.screenGroupBox.Controls.Add(this.videoSource1Selection);
            this.screenGroupBox.Controls.Add(this.screen2Label);
            this.screenGroupBox.Controls.Add(this.screen1Label);
            this.screenGroupBox.Location = new System.Drawing.Point(251, 446);
            this.screenGroupBox.Name = "screenGroupBox";
            this.screenGroupBox.Size = new System.Drawing.Size(235, 77);
            this.screenGroupBox.TabIndex = 10;
            this.screenGroupBox.TabStop = false;
            this.screenGroupBox.Text = "Screen Selection";
            // 
            // videoSource2Selection
            // 
            this.videoSource2Selection.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.videoSource2Selection.FormattingEnabled = true;
            this.videoSource2Selection.Items.AddRange(new object[] {
            "Source Video",
            "Thresholded Video",
            "Marker Detection"});
            this.videoSource2Selection.Location = new System.Drawing.Point(93, 48);
            this.videoSource2Selection.Name = "videoSource2Selection";
            this.videoSource2Selection.Size = new System.Drawing.Size(136, 21);
            this.videoSource2Selection.TabIndex = 11;
            this.videoSource2Selection.SelectedIndexChanged += new System.EventHandler(this.videoSource2Selection_SelectedIndexChanged);
            // 
            // videoSource1Selection
            // 
            this.videoSource1Selection.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.videoSource1Selection.FormattingEnabled = true;
            this.videoSource1Selection.Items.AddRange(new object[] {
            "Source Video",
            "Thresholded Video",
            "Marker Detection"});
            this.videoSource1Selection.Location = new System.Drawing.Point(93, 21);
            this.videoSource1Selection.Name = "videoSource1Selection";
            this.videoSource1Selection.Size = new System.Drawing.Size(136, 21);
            this.videoSource1Selection.TabIndex = 10;
            this.videoSource1Selection.SelectedIndexChanged += new System.EventHandler(this.videoSource1Selection_SelectedIndexChanged);
            // 
            // screen2Label
            // 
            this.screen2Label.Location = new System.Drawing.Point(6, 53);
            this.screen2Label.Name = "screen2Label";
            this.screen2Label.Size = new System.Drawing.Size(81, 17);
            this.screen2Label.TabIndex = 9;
            this.screen2Label.Text = "Screen 2:";
            // 
            // screen1Label
            // 
            this.screen1Label.Location = new System.Drawing.Point(6, 24);
            this.screen1Label.Name = "screen1Label";
            this.screen1Label.Size = new System.Drawing.Size(81, 26);
            this.screen1Label.TabIndex = 8;
            this.screen1Label.Text = "Screen 1: ";
            // 
            // screenViewer2Button
            // 
            this.screenViewer2Button.Enabled = false;
            this.screenViewer2Button.Location = new System.Drawing.Point(991, 10);
            this.screenViewer2Button.Name = "screenViewer2Button";
            this.screenViewer2Button.Size = new System.Drawing.Size(50, 23);
            this.screenViewer2Button.TabIndex = 11;
            this.screenViewer2Button.Text = "Detach";
            this.screenViewer2Button.UseVisualStyleBackColor = true;
            this.screenViewer2Button.Click += new System.EventHandler(this.screenViewer2Button_Click);
            // 
            // screenViewer1Button
            // 
            this.screenViewer1Button.Enabled = false;
            this.screenViewer1Button.Location = new System.Drawing.Point(474, 10);
            this.screenViewer1Button.Name = "screenViewer1Button";
            this.screenViewer1Button.Size = new System.Drawing.Size(50, 23);
            this.screenViewer1Button.TabIndex = 12;
            this.screenViewer1Button.Text = "Detach";
            this.screenViewer1Button.UseVisualStyleBackColor = true;
            this.screenViewer1Button.Click += new System.EventHandler(this.screenViewer1Button_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.thresholdMethodLabel);
            this.groupBox1.Controls.Add(this.thresholdCreateLabel);
            this.groupBox1.Controls.Add(this.thresholdOpenLabel);
            this.groupBox1.Controls.Add(this.trainingFileLabel);
            this.groupBox1.Location = new System.Drawing.Point(492, 446);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(293, 77);
            this.groupBox1.TabIndex = 13;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Treshold Training";
            // 
            // thresholdMethodLabel
            // 
            this.thresholdMethodLabel.Location = new System.Drawing.Point(6, 53);
            this.thresholdMethodLabel.Name = "thresholdMethodLabel";
            this.thresholdMethodLabel.Size = new System.Drawing.Size(152, 18);
            this.thresholdMethodLabel.TabIndex = 12;
            this.thresholdMethodLabel.Text = "Threshold Method: Otsu";
            // 
            // thresholdCreateLabel
            // 
            this.thresholdCreateLabel.Location = new System.Drawing.Point(164, 48);
            this.thresholdCreateLabel.Name = "thresholdCreateLabel";
            this.thresholdCreateLabel.Size = new System.Drawing.Size(123, 23);
            this.thresholdCreateLabel.TabIndex = 11;
            this.thresholdCreateLabel.Text = "Create";
            this.thresholdCreateLabel.UseVisualStyleBackColor = true;
            this.thresholdCreateLabel.Click += new System.EventHandler(this.thresholdCreateLabel_Click);
            // 
            // thresholdOpenLabel
            // 
            this.thresholdOpenLabel.Location = new System.Drawing.Point(164, 19);
            this.thresholdOpenLabel.Name = "thresholdOpenLabel";
            this.thresholdOpenLabel.Size = new System.Drawing.Size(123, 23);
            this.thresholdOpenLabel.TabIndex = 10;
            this.thresholdOpenLabel.Text = "Open";
            this.thresholdOpenLabel.UseVisualStyleBackColor = true;
            this.thresholdOpenLabel.Click += new System.EventHandler(this.thresholdOpenLabel_Click);
            // 
            // trainingFileLabel
            // 
            this.trainingFileLabel.Location = new System.Drawing.Point(6, 24);
            this.trainingFileLabel.Name = "trainingFileLabel";
            this.trainingFileLabel.Size = new System.Drawing.Size(152, 18);
            this.trainingFileLabel.TabIndex = 9;
            this.trainingFileLabel.Text = "Training File: No File Loaded";
            // 
            // captureScreen1
            // 
            this.captureScreen1.Enabled = false;
            this.captureScreen1.Location = new System.Drawing.Point(416, 10);
            this.captureScreen1.Name = "captureScreen1";
            this.captureScreen1.Size = new System.Drawing.Size(52, 23);
            this.captureScreen1.TabIndex = 14;
            this.captureScreen1.Text = "Capture";
            this.captureScreen1.UseVisualStyleBackColor = true;
            this.captureScreen1.Click += new System.EventHandler(this.captureScreen1_Click);
            // 
            // captureScreen2
            // 
            this.captureScreen2.Enabled = false;
            this.captureScreen2.Location = new System.Drawing.Point(933, 10);
            this.captureScreen2.Name = "captureScreen2";
            this.captureScreen2.Size = new System.Drawing.Size(52, 23);
            this.captureScreen2.TabIndex = 15;
            this.captureScreen2.Text = "Capture";
            this.captureScreen2.UseVisualStyleBackColor = true;
            this.captureScreen2.Click += new System.EventHandler(this.captureScreen2_Click);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size(1053, 533);
            this.Controls.Add(this.captureScreen2);
            this.Controls.Add(this.captureScreen1);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.screenViewer1Button);
            this.Controls.Add(this.screenViewer2Button);
            this.Controls.Add(this.screenGroupBox);
            this.Controls.Add(this.trainingGroupBox);
            this.Controls.Add(this.permittedCodesLabel);
            this.Controls.Add(this.cameraFpsLabel);
            this.Controls.Add(this.videoSourcePlayer2);
            this.Controls.Add(this.cameraCombo);
            this.Controls.Add(this.videoSourcePlayer1);
            this.Controls.Add(this.stopButton);
            this.Controls.Add(this.startButton);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.Name = "MainForm";
            this.Text = "Ceramic Marker Detector";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainForm_FormClosing);
            this.trainingGroupBox.ResumeLayout(false);
            this.screenGroupBox.ResumeLayout(false);
            this.groupBox1.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button startButton;
        private System.Windows.Forms.Button stopButton;
        private System.Windows.Forms.Timer timer;
        private System.Windows.Forms.Button addCodesButton;
        private System.Windows.Forms.Button clearCodesButton;
        private System.Windows.Forms.Button removeCodesButton;
        private System.Windows.Forms.ComboBox cameraCombo;
        private AForge.Controls.VideoSourcePlayer videoSourcePlayer1;
        private AForge.Controls.VideoSourcePlayer videoSourcePlayer2;
        private System.Windows.Forms.Label cameraFpsLabel;
        private System.Windows.Forms.Label permittedCodesLabel;
        private System.Windows.Forms.GroupBox trainingGroupBox;
        private System.Windows.Forms.GroupBox screenGroupBox;
        private System.Windows.Forms.ComboBox videoSource2Selection;
        private System.Windows.Forms.ComboBox videoSource1Selection;
        private System.Windows.Forms.Label screen2Label;
        private System.Windows.Forms.Label screen1Label;
        private System.Windows.Forms.Button screenViewer2Button;
        private System.Windows.Forms.Button screenViewer1Button;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Button thresholdCreateLabel;
        private System.Windows.Forms.Button thresholdOpenLabel;
        private System.Windows.Forms.Label trainingFileLabel;
        private System.Windows.Forms.Label thresholdMethodLabel;
        private System.Windows.Forms.Button captureScreen1;
        private System.Windows.Forms.Button captureScreen2;
    }
}

