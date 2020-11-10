/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import uk.ac.horizon.artcodes.model.Action;

public final class Analytics
{
	public enum Target
	{
		APP,
		// Add more trackers here if you need, and update the code in #get(Target) below
	}

	private static final String CONTENT_TYPE = "Experience";
	private static final String SCAN_EVENT = "scan_content";
	private static final String STAR_EVENT = "star_content";
	private static final String UNSTAR_EVENT = "unstar_content";
	private static final String OPEN_SCAN_EVENT = "open_content";
	private static FirebaseAnalytics analytics;

	static synchronized void initialize(Context context)
	{
		if (analytics != null)
		{
			throw new IllegalStateException("Extra call to initialize analytics trackers");
		}

		analytics = FirebaseAnalytics.getInstance(context);
	}

	public static void trackException(Throwable e)
	{
		Log.e("Google Analytics", e.getMessage(), e);
		//get(Target.APP).send(new HitBuilders.ExceptionBuilder()
		//		.setDescription(Throwables.getStackTraceAsString(e))
		//		.build());
	}

	public static void logScan(String experience, Action action)
	{
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action.getName());
		//bundle.putString(FirebaseAnalytics.Param.);
		analytics.logEvent(SCAN_EVENT, bundle);
	}

	public static void logOpenScan(String experience, Action action)
	{
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action.getName());
		bundle.putString("URL", action.getUrl());
		analytics.logEvent(OPEN_SCAN_EVENT, bundle);
	}

	public static void logStar(String experience) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		analytics.logEvent(STAR_EVENT, bundle);
	}

	public static void logUnstar(String experience) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		analytics.logEvent(UNSTAR_EVENT, bundle);
	}

	public static void logSelect(String experience)
	{
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
	}

	public static void logShare(String experience)
	{
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, experience);
		analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
	}

	public static void logSearch(String searchTerm)
	{
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm);
		analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
	}
}
