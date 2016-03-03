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

package uk.ac.horizon.artcodes.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.auth.UserRecoverableAuthException;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.adapter.ExperienceSortedListAdapter;
import uk.ac.horizon.artcodes.databinding.ListBinding;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceLibraryFragment extends ArtcodeFragmentBase
{
	private ExperienceAdapter adapter;
	private Account account;

	private Drawable getTintedDrawable(@DrawableRes int drawable, @ColorInt int color)
	{
		final Drawable original = ContextCompat.getDrawable(getContext(), drawable);
		if(original != null)
		{
			final Drawable wrapDrawable = DrawableCompat.wrap(original);
			DrawableCompat.setTint(wrapDrawable, color);
			return wrapDrawable;
		}
		return null;
	}

	@NonNull
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		adapter = new ExperienceSortedListAdapter(getActivity(), getServer());
		adapter.setFabPadding(true);
		binding.setAdapter(adapter);

		binding.emptyIcon.setImageResource(R.drawable.ic_folder_144dp);
		binding.emptyText.setText(R.string.empty);
		binding.emptyDetail.setText(R.string.emptyHint);

		final FloatingActionButton fab = new FloatingActionButton(getContext());
		fab.setImageDrawable(getTintedDrawable(R.drawable.ic_add_24dp, Color.WHITE));
		//fab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.apptheme_accent));
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ExperienceEditActivity.start(getActivity(), new Experience(), account);
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			fab.setElevation(4);
		}

		final int spacing = getResources().getDimensionPixelSize(R.dimen.padding);
		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
		layoutParams.setMargins(spacing, spacing, spacing, spacing);

		fab.setLayoutParams(layoutParams);

		((ViewGroup) binding.getRoot()).addView(fab);

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Library");
		final Account account = getAccount();
		getActivity().setTitle(account.getDisplayName());
		adapter.loadStarted();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					account.validates();

					account.loadLibrary(adapter);
				}
				catch (UserRecoverableAuthException e)
				{
					startActivityForResult(e.getIntent(), 1);
				}
			}
		}).start();

	}

	private Account getAccount()
	{
		if (account == null)
		{
			if (getArguments() != null)
			{
				String accountID = getArguments().getString("account");
				if (accountID != null)
				{
					Account selected = getServer().getAccount(accountID);
					if (selected != null)
					{
						account = selected;
					}
				}
			}
		}
		return account;
	}
}
