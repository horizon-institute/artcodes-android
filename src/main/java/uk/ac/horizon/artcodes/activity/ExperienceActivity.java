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

package uk.ac.horizon.artcodes.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.util.List;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.AccountItemBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceBinding;
import uk.ac.horizon.artcodes.databinding.LocationItemBinding;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceActivity extends ExperienceActivityBase
{
	private ExperienceBinding binding;

	public static void start(Context context, Experience experience)
	{
		Intent intent = new Intent(context, ExperienceActivity.class);
		intent.putExtra("experience", new Gson().toJson(experience));
		context.startActivity(intent);
	}

	public void editExperience(View view)
	{
		Account account = getAccount();
		if (account != null)
		{
			ExperienceEditActivity.start(this, getExperience(), account);
		}
	}

	@Override
	public void loaded(Experience experience)
	{
		super.loaded(experience);
		GoogleAnalytics.trackScreen("View Experience", experience.getId());
		binding.setExperience(experience);

//		if (Feature.get(this, R.bool.feature_history).isEnabled())
//		{
//			binding.experienceHistoryButton.setVisibility(View.VISIBLE);
//		}

		binding.experienceDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if (binding.experienceDescription.getLineCount() > 1)
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					{
						binding.experienceDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					final Layout layout = binding.experienceDescription.getLayout();
					if (layout != null)
					{
						final int lines = layout.getLineCount();
						int ellipsisCount = 0;
						for (int index = 0; index < lines; index++)
						{
							ellipsisCount += layout.getEllipsisCount(index);
						}

						Log.i("Ellipsis", "Lines = " + lines + ", ellipsis = " + ellipsisCount);
						if (ellipsisCount == 0)
						{
							binding.experienceDescriptionMore.setVisibility(View.GONE);
						}
						else
						{
							final int lineChars = layout.getLineStart(1);
							if (ellipsisCount < (lineChars * 2))
							{
								binding.experienceDescription.setMaxLines(Integer.MAX_VALUE);
								binding.experienceDescriptionMore.setVisibility(View.GONE);
							}
							else
							{
								binding.experienceDescriptionMore.setVisibility(View.VISIBLE);
							}
						}
					}
				}
				else
				{
					binding.experienceDescriptionMore.setVisibility(View.GONE);
				}
			}
		});

		binding.experienceLocations.removeAllViews();
		for (final Availability availability : experience.getAvailabilities())
		{
			if (availability.getName() != null && availability.getLat() != null && availability.getLon() != null)
			{
				final LocationItemBinding locationBinding = LocationItemBinding.inflate(getLayoutInflater(), binding.experienceLocations, false);
				locationBinding.setAvailability(availability);
				locationBinding.getRoot().setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						final Uri gmmIntentUri = Uri.parse("geo:" + availability.getLat() + "," + availability.getLon());
						final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
						mapIntent.setPackage("com.google.android.apps.maps");
						startActivity(mapIntent);
					}
				});
				binding.experienceLocations.addView(locationBinding.getRoot());
			}
		}

//		if(experience.getOriginalID() != null)
//		{
//			getServer().loadExperience(experience.getOriginalID(), new LoadCallback<Experience>()
//			{
//				@Override
//				public void loaded(Experience item)
//				{
//					TextView view = new TextView(ExperienceActivity.this);
//					view.setText("Copy of " + item.getDisplayName());
//					binding.experienceLocations.addView(view);
//				}
//			});
//		}

		if (updateActions())
		{
			LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					if (intent.hasExtra("experience"))
					{
						loaded(new Gson().fromJson(intent.getStringExtra("experience"), Experience.class));
					}
				}
			}, new IntentFilter(getUri()));
		}
		updateStarred();
	}

	public void readDescription(View view)
	{
		// TODO Animate
		binding.experienceDescriptionMore.setVisibility(View.GONE);
		binding.experienceDescription.setMaxLines(Integer.MAX_VALUE);
		binding.scrollView.smoothScrollTo(0, binding.experienceDescription.getTop());
	}

	public void scanExperience(View view)
	{
		ArtcodeActivity.start(this, getExperience());
	}

	public void shareExperience(View view)
	{
		GoogleAnalytics.trackEvent("Experience", "Share", getUri());
		startActivity(ShareCompat.IntentBuilder.from(this)
				.setType("text/plain")
				.setText(getUri())
				.setSubject(getExperience().getName())
				.createChooserIntent());
	}

	public void starExperience(View view)
	{
		getServer().loadStarred(new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> starred)
			{
				if (starred.contains(getUri()))
				{
					GoogleAnalytics.trackEvent("Experience", "Unstar", getUri());
					starred.remove(getUri());
					getServer().saveStarred(starred);
				}
				else
				{
					GoogleAnalytics.trackEvent("Experience", "Star", getUri());
					starred.add(getUri());
					getServer().saveStarred(starred);
				}
				updateStarred();
			}
		});
	}

	public void copyExperience(View view)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		linearLayout.setLayoutParams(layoutParams);
		builder.setTitle(R.string.copy);
		builder.setView(linearLayout);
		final Dialog dialog = builder.create();

		for (final Account account : getServer().getAccounts())
		{
			if (!account.canEdit(getUri()))
			{
				Log.i("copy", "Added " + account.getId());
				AccountItemBinding binding = AccountItemBinding.inflate(getLayoutInflater(), linearLayout, false);
				binding.setAccount(account);
				binding.getRoot().setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						final Experience experience = getExperience();
						if (experience.getId() != null && (experience.getId().startsWith("http://") || experience.getId().startsWith("https://")))
						{
							experience.setOriginalID(experience.getId());
						}
						experience.setId(null);
						experience.setName(getString(R.string.copy_of, experience.getName()));
						account.saveExperience(experience);
						ExperienceActivity.start(ExperienceActivity.this, experience);
					}
				});
				linearLayout.addView(binding.getRoot());
			}
		}

		dialog.show();
	}

	public void startExperienceHistory(View view)
	{
		ExperienceHistoryActivity.start(this, getExperience());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.experience);

		onNewIntent(getIntent());

		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	private boolean updateActions()
	{
		boolean copiable = false;
		boolean editable = false;
		boolean saving = false;
		for (Account account : getServer().getAccounts())
		{
			if (account.canEdit(getUri()))
			{
				editable = true;
			}
			else
			{
				copiable = true;
			}

			if (account.isSaving(getUri()))
			{
				saving = true;
			}
		}

		setVisible(binding.experienceEditButton, editable);
		setVisible(binding.experienceCopyButton, copiable);
		setVisible(binding.saveProgress, saving);
		setVisible(binding.buttonBar, !saving);
		return saving;
	}

	private Account getAccount()
	{
		for (Account account : getServer().getAccounts())
		{
			if (account.canEdit(getUri()))
			{
				return account;
			}
		}
		return null;
	}

	private void setVisible(View view, boolean visible)
	{
		if (visible)
		{
			view.setVisibility(View.VISIBLE);
		}
		else
		{
			view.setVisibility(View.GONE);
		}
	}

	private void updateStarred()
	{
		getServer().loadStarred(new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				if (item.contains(getUri()))
				{
					binding.experienceFavouriteButton.setText(R.string.unstar);
					binding.experienceFavouriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_black_24dp, 0, 0);
				}
				else
				{
					binding.experienceFavouriteButton.setText(R.string.star);
					binding.experienceFavouriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_border_black_24dp, 0, 0);
				}
			}
		});
	}
}
