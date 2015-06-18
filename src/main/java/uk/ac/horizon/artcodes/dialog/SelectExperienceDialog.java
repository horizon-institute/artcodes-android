/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.artcodes.model.Experience;

import java.util.ArrayList;
import java.util.List;

public class SelectExperienceDialog extends AppCompatDialog
{
	private class ExperienceHolder extends RecyclerView.ViewHolder
	{
		//private final CheckBox mSolvedCheckBox;
		//private Crime mCrime;

		public ExperienceHolder(View itemView)
		{
			super(itemView);

			//mSolvedCheckBox = (CheckBox) itemView
			//		.findViewById(R.id.crime_list_item_solvedCheckBox);
		}
	}

	private class ExperienceAdapter extends RecyclerView.Adapter<ExperienceHolder>
	{
		private List<Experience> recent = new ArrayList<>();
		private List<Experience> recommended = new ArrayList<>();

		@Override
		public ExperienceHolder onCreateViewHolder(ViewGroup parent, int pos)
		{
			if(getItemViewType(pos) == 1)
			{
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.experience_select_item, parent, false);
			}
			//View view = LayoutInflater.from(parent.getContext())
			//		.inflate(R.layout.list_item_crime, parent, false);
			return null;
		}

		@Override
		public void onBindViewHolder(ExperienceHolder holder, int pos)
		{

			//Crime crime = mCrimes.get(pos);
			//holder.bindCrime(crime);
		}

		@Override
		public int getItemViewType(int position)
		{
			if(!recent.isEmpty() && position == 0)
			{
				return 2;
			}
			else if(!recommended.isEmpty())
			{
				if(recent.isEmpty())
				{
					if(position == 0)
					{
						return 2;
					}
				}
				else if(position == (recent.size() + 1))
				{
					return 2;
				}
			}
			return 1;
		}

		@Override
		public int getItemCount()
		{
			final int recentSize = recent.isEmpty() ? 0 : recent.size() + 1;
			final int recommendedSize =  recommended.isEmpty() ? 0 : recommended.size() + 1;


			return recentSize + recommendedSize;
		}
	}


	private RecyclerView recyclerView;

	public SelectExperienceDialog(Context context)
	{
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.experience_select);

		//recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		//recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		//recyclerView.setAdapter(new CrimeAdapter());

	}
}
