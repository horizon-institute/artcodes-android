/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.scanner;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class TextAnimator
{
	private final Handler settingsFeedbackHandler = new Handler();
	private final Runnable settingsFeedbackRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			view.setVisibility(View.GONE);
		}
	};
	private final TextView view;

	public TextAnimator(TextView view)
	{
		this.view = view;
	}

	public void setText(int text)
	{
		view.setText(text);
		view.setVisibility(View.VISIBLE);
		settingsFeedbackHandler.removeCallbacks(settingsFeedbackRunnable);
		settingsFeedbackHandler.postDelayed(settingsFeedbackRunnable, 3000);
	}
}
