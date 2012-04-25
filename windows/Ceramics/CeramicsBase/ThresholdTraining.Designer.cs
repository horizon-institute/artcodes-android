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
    partial class ThresholdTraining
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
            this.loadDataButton = new System.Windows.Forms.Button();
            this.imageDataGrid = new System.Windows.Forms.DataGridView();
            this.sourceImage = new System.Windows.Forms.DataGridViewImageColumn();
            this.maskImage = new System.Windows.Forms.DataGridViewImageColumn();
            this.createMapButton = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.imageDataGrid)).BeginInit();
            this.SuspendLayout();
            // 
            // loadDataButton
            // 
            this.loadDataButton.Location = new System.Drawing.Point(12, 280);
            this.loadDataButton.Name = "loadDataButton";
            this.loadDataButton.Size = new System.Drawing.Size(177, 36);
            this.loadDataButton.TabIndex = 1;
            this.loadDataButton.Text = "Load Training Images";
            this.loadDataButton.UseVisualStyleBackColor = true;
            this.loadDataButton.Click += new System.EventHandler(this.loadDataButton_Click);
            // 
            // imageDataGrid
            // 
            this.imageDataGrid.AllowUserToAddRows = false;
            this.imageDataGrid.AllowUserToDeleteRows = false;
            this.imageDataGrid.AllowUserToResizeColumns = false;
            this.imageDataGrid.AllowUserToResizeRows = false;
            this.imageDataGrid.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.imageDataGrid.AutoSizeRowsMode = System.Windows.Forms.DataGridViewAutoSizeRowsMode.AllCells;
            this.imageDataGrid.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.imageDataGrid.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.sourceImage,
            this.maskImage});
            this.imageDataGrid.EditMode = System.Windows.Forms.DataGridViewEditMode.EditProgrammatically;
            this.imageDataGrid.Location = new System.Drawing.Point(12, 12);
            this.imageDataGrid.MultiSelect = false;
            this.imageDataGrid.Name = "imageDataGrid";
            this.imageDataGrid.ReadOnly = true;
            this.imageDataGrid.RowHeadersVisible = false;
            this.imageDataGrid.RowHeadersWidthSizeMode = System.Windows.Forms.DataGridViewRowHeadersWidthSizeMode.DisableResizing;
            this.imageDataGrid.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.imageDataGrid.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.CellSelect;
            this.imageDataGrid.Size = new System.Drawing.Size(360, 262);
            this.imageDataGrid.TabIndex = 2;
            // 
            // sourceImage
            // 
            this.sourceImage.HeaderText = "Source Image";
            this.sourceImage.Name = "sourceImage";
            this.sourceImage.ReadOnly = true;
            // 
            // maskImage
            // 
            this.maskImage.HeaderText = "Mask Image";
            this.maskImage.Name = "maskImage";
            this.maskImage.ReadOnly = true;
            // 
            // createMapButton
            // 
            this.createMapButton.Location = new System.Drawing.Point(195, 280);
            this.createMapButton.Name = "createMapButton";
            this.createMapButton.Size = new System.Drawing.Size(177, 36);
            this.createMapButton.TabIndex = 3;
            this.createMapButton.Text = "Create Training Data";
            this.createMapButton.UseVisualStyleBackColor = true;
            this.createMapButton.Click += new System.EventHandler(this.createMapButton_Click);
            // 
            // ThresholdTraining
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(384, 328);
            this.Controls.Add(this.createMapButton);
            this.Controls.Add(this.imageDataGrid);
            this.Controls.Add(this.loadDataButton);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.Name = "ThresholdTraining";
            this.Text = "Threshold Training";
            ((System.ComponentModel.ISupportInitialize)(this.imageDataGrid)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button loadDataButton;
        private System.Windows.Forms.DataGridView imageDataGrid;
        private System.Windows.Forms.DataGridViewImageColumn sourceImage;
        private System.Windows.Forms.DataGridViewImageColumn maskImage;
        private System.Windows.Forms.Button createMapButton;
    }
}