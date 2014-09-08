/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import uk.ac.horizon.aestheticodes.R;

public abstract class DrawerActivity extends ActionBarActivity
{
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
		{
			selectItem(position);
		}
	}

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private CharSequence drawerTitle;
	private ListView drawerList;

	private CharSequence mTitle;

	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	protected ListView getListView()
	{
		return drawerList;
	}

	protected void createDrawer(ListAdapter adapter, int drawerLayoutID, int drawerListID)
	{
		Log.i("", "Creating drawer");
		drawerLayout = (DrawerLayout) findViewById(drawerLayoutID);
		drawerList = (ListView) findViewById(drawerListID);
		Log.i("", "Creating drawer " + drawerLayout + ", " + drawerList);
		drawerList.setAdapter(adapter);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());

		drawerTitle = "Experiences";
		mTitle = getTitle();

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(final View view)
			{
				getSupportActionBar().setTitle(mTitle);
			}

			@Override
			public void onDrawerOpened(final View drawerView)
			{
				getSupportActionBar().setTitle(drawerTitle);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	/**
	 * Swaps fragments in the main content view
	 */
	protected void selectItem(final int position)
	{
		drawerList.setSelection(position);
		drawerLayout.closeDrawer(drawerList);
	}

	protected void setItemSelected(final int position)
	{
		drawerList.setSelection(position);
	}

	@Override
	public void setTitle(final CharSequence title)
	{
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}
}