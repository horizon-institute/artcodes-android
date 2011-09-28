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
﻿namespace CeramicsLauncher
{
    partial class Form1
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
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.localVersionLabel = new System.Windows.Forms.Label();
            this.remoteVersionLabel = new System.Windows.Forms.Label();
            this.lineSeparator = new System.Windows.Forms.Label();
            this.updateLaterButton = new System.Windows.Forms.Button();
            this.updateButton = new System.Windows.Forms.Button();
            this.updateProgressBar = new System.Windows.Forms.ProgressBar();
            this.updateInProgressButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 15.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(12, 9);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(309, 60);
            this.label1.TabIndex = 0;
            this.label1.Text = "A new version of the Ceramic Marker Detector is available.";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(12, 81);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(128, 20);
            this.label2.TabIndex = 1;
            this.label2.Text = "Current Version: ";
            this.label2.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label3.Location = new System.Drawing.Point(12, 105);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(166, 20);
            this.label3.TabIndex = 2;
            this.label3.Text = "Most Recent Version: ";
            this.label3.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // localVersionLabel
            // 
            this.localVersionLabel.AutoSize = true;
            this.localVersionLabel.Font = new System.Drawing.Font("Courier New", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.localVersionLabel.Location = new System.Drawing.Point(193, 81);
            this.localVersionLabel.Name = "localVersionLabel";
            this.localVersionLabel.Size = new System.Drawing.Size(120, 21);
            this.localVersionLabel.TabIndex = 3;
            this.localVersionLabel.Text = "1.0.0.1000";
            this.localVersionLabel.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // remoteVersionLabel
            // 
            this.remoteVersionLabel.AutoSize = true;
            this.remoteVersionLabel.Font = new System.Drawing.Font("Courier New", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.remoteVersionLabel.Location = new System.Drawing.Point(193, 105);
            this.remoteVersionLabel.Name = "remoteVersionLabel";
            this.remoteVersionLabel.Size = new System.Drawing.Size(120, 21);
            this.remoteVersionLabel.TabIndex = 4;
            this.remoteVersionLabel.Text = "1.0.0.1000";
            this.remoteVersionLabel.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // lineSeparator
            // 
            this.lineSeparator.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.lineSeparator.Location = new System.Drawing.Point(12, 135);
            this.lineSeparator.Name = "lineSeparator";
            this.lineSeparator.Size = new System.Drawing.Size(309, 2);
            this.lineSeparator.TabIndex = 5;
            // 
            // updateLaterButton
            // 
            this.updateLaterButton.Location = new System.Drawing.Point(12, 146);
            this.updateLaterButton.Name = "updateLaterButton";
            this.updateLaterButton.Size = new System.Drawing.Size(150, 32);
            this.updateLaterButton.TabIndex = 6;
            this.updateLaterButton.Text = "Update Later";
            this.updateLaterButton.UseVisualStyleBackColor = true;
            this.updateLaterButton.Click += new System.EventHandler(this.updateLaterButton_Click);
            // 
            // updateButton
            // 
            this.updateButton.Location = new System.Drawing.Point(171, 146);
            this.updateButton.Name = "updateButton";
            this.updateButton.Size = new System.Drawing.Size(150, 32);
            this.updateButton.TabIndex = 7;
            this.updateButton.Text = "Update Now";
            this.updateButton.UseVisualStyleBackColor = true;
            this.updateButton.Click += new System.EventHandler(this.updateButton_Click);
            // 
            // updateProgressBar
            // 
            this.updateProgressBar.Location = new System.Drawing.Point(12, 110);
            this.updateProgressBar.Name = "updateProgressBar";
            this.updateProgressBar.Size = new System.Drawing.Size(309, 21);
            this.updateProgressBar.Style = System.Windows.Forms.ProgressBarStyle.Continuous;
            this.updateProgressBar.TabIndex = 8;
            this.updateProgressBar.Visible = false;
            // 
            // updateInProgressButton
            // 
            this.updateInProgressButton.Enabled = false;
            this.updateInProgressButton.Location = new System.Drawing.Point(91, 146);
            this.updateInProgressButton.Name = "updateInProgressButton";
            this.updateInProgressButton.Size = new System.Drawing.Size(150, 32);
            this.updateInProgressButton.TabIndex = 9;
            this.updateInProgressButton.Text = "Launch Ceramics";
            this.updateInProgressButton.UseVisualStyleBackColor = true;
            this.updateInProgressButton.Visible = false;
            this.updateInProgressButton.Click += new System.EventHandler(this.updateInProgressButton_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(333, 190);
            this.Controls.Add(this.updateInProgressButton);
            this.Controls.Add(this.updateProgressBar);
            this.Controls.Add(this.updateButton);
            this.Controls.Add(this.updateLaterButton);
            this.Controls.Add(this.lineSeparator);
            this.Controls.Add(this.remoteVersionLabel);
            this.Controls.Add(this.localVersionLabel);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.Name = "Form1";
            this.Text = "Ceramics Updater";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label localVersionLabel;
        private System.Windows.Forms.Label remoteVersionLabel;
        private System.Windows.Forms.Label lineSeparator;
        private System.Windows.Forms.Button updateLaterButton;
        private System.Windows.Forms.Button updateButton;
        private System.Windows.Forms.ProgressBar updateProgressBar;
        private System.Windows.Forms.Button updateInProgressButton;

    }
}

