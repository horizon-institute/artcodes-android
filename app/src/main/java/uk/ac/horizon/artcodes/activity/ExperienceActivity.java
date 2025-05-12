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
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.List;

import uk.ac.horizon.artcodes.Analytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.AccountItemBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceBinding;
import uk.ac.horizon.artcodes.databinding.LocationItemBinding;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.ScanEvent;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceActivity extends ExperienceActivityBase {
	private ExperienceBinding binding;
	private Experience originalExperience;

	public static void start(Context context, Experience experience) {
		context.startActivity(intent(context, experience));
	}

	public static Intent intent(Context context, Experience experience) {
		Intent intent = new Intent(context, ExperienceActivity.class);
		//intent.putExtra("experience", new Gson().toJson(experience));
		StaticActivityMessage.experience = experience;
		return intent;
	}

	public void openOriginalExperience(View view) {
		ExperienceActivity.start(this, originalExperience);
	}

	public void editExperience(View view) {
		Account account = getAccount();
		if (account != null) {
			ExperienceEditActivity.start(this, getExperience(), account);
		}
	}

	@Override
	public void loaded(Experience experience) {
		super.loaded(experience);
		binding.setExperience(experience);

		binding.experienceDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (binding.experienceDescription.getLineCount() > 1) {
					binding.experienceDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					final Layout layout = binding.experienceDescription.getLayout();
					if (layout != null) {
						final int lines = layout.getLineCount();
						int ellipsisCount = 0;
						for (int index = 0; index < lines; index++) {
							ellipsisCount += layout.getEllipsisCount(index);
						}

						Log.i("Ellipsis", "Lines = " + lines + ", ellipsis = " + ellipsisCount);
						if (ellipsisCount == 0) {
							binding.experienceDescriptionMore.setVisibility(View.GONE);
						} else {
							final int lineChars = layout.getLineStart(1);
							if (ellipsisCount < (lineChars * 2)) {
								binding.experienceDescription.setMaxLines(Integer.MAX_VALUE);
								binding.experienceDescriptionMore.setVisibility(View.GONE);
							} else {
								binding.experienceDescriptionMore.setVisibility(View.VISIBLE);
							}
						}
					}
				} else {
					binding.experienceDescriptionMore.setVisibility(View.GONE);
				}
			}
		});

		binding.experienceLocations.removeAllViews();
		for (final Availability availability : experience.getAvailabilities()) {
			if (availability.getName() != null && availability.getLat() != null && availability.getLon() != null) {
				final LocationItemBinding locationBinding = LocationItemBinding.inflate(getLayoutInflater(), binding.experienceLocations, false);
				locationBinding.setAvailability(availability);
				locationBinding.getRoot().setOnClickListener(v -> {
					final Uri gmmIntentUri = Uri.parse("geo:" + availability.getLat() + "," + availability.getLon() + "?q=" + availability.getAddress());
					final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
					mapIntent.setPackage("com.google.android.apps.maps");
					startActivity(mapIntent);
				});
				binding.experienceLocations.addView(locationBinding.getRoot());
			}
		}

		if (experience.getOriginalID() != null) {
			getServer().loadExperience(experience.getOriginalID(), new LoadCallback<>() {
				@Override
				public void loaded(Experience item) {
					originalExperience = item;
					binding.setOriginalExperience(item);
				}

				@Override
				public void error(Throwable e) {

				}
			});
		}

		if (updateActions()) {
			LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.hasExtra("experience")) {
						loaded(new Gson().fromJson(intent.getStringExtra("experience"), Experience.class));
					}
				}
			}, new IntentFilter(getUri()));
		}
		updateStarred();
	}

	public void readDescription(View view) {
		// TODO Animate
		binding.experienceDescriptionMore.setVisibility(View.GONE);
		binding.experienceDescription.setMaxLines(Integer.MAX_VALUE);
		binding.scrollView.smoothScrollTo(0, binding.experienceDescription.getTop());
	}

	public void scanExperience(View view) {
		ArtcodeActivity.start(this, getExperience());
	}

	public void shareExperience(View view) {
		Analytics.logShare(getUri());

		startActivity(new ShareCompat.IntentBuilder(this)
				.setType("text/plain")
				.setText(getUri())
				.setSubject(getExperience().getName())
				.createChooserIntent());
	}

	public void starExperience(View view) {
		getServer().loadStarred(new LoadCallback<>() {
			@Override
			public void loaded(List<String> starred) {
				if (starred.contains(getUri())) {
					Analytics.logUnstar(getUri());
					starred.remove(getUri());
					getServer().saveStarred(starred);
				} else {
					Analytics.logStar(getUri());
					starred.add(getUri());
					getServer().saveStarred(starred);
				}
				updateStarred();
			}

			@Override
			public void error(Throwable e) {
				Analytics.trackException(e);
			}
		});
	}

	public void copyExperience(View view) {
		if (getExperience().getCanCopy() != null && !getExperience().getCanCopy()) {
			return;
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		linearLayout.setLayoutParams(layoutParams);
		builder.setTitle(R.string.copy);
		builder.setView(linearLayout);
		final Dialog dialog = builder.create();

		for (final Account account : getServer().getAccounts()) {
			if (!account.canEdit(getUri())) {
				Log.i("copy", "Added " + account.getId());
				AccountItemBinding binding = AccountItemBinding.inflate(getLayoutInflater(), linearLayout, false);
				binding.setAccount(account);
				binding.getRoot().setOnClickListener(v -> {
					dialog.dismiss();
					final Experience experience = getExperience();
					if (experience.getId() != null && (experience.getId().startsWith("http://") || experience.getId().startsWith("https://"))) {
						experience.setOriginalID(experience.getId());
					}
					experience.setId(null);
					experience.setName(getString(R.string.copy_of, experience.getName()));
					experience.getAvailabilities().clear();
					account.saveExperience(experience);
					ExperienceActivity.start(ExperienceActivity.this, experience);
				});
				linearLayout.addView(binding.getRoot());
			}
		}

		dialog.show();
	}

	public void startExperienceHistory(View view) {
		ExperienceHistoryActivity.start(this, getExperience());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		binding = DataBindingUtil.setContentView(this, R.layout.experience);

		onNewIntent(getIntent());

		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	private boolean updateActions() {
		boolean copiable = false;
		boolean editable = false;
		boolean saving = false;
		for (Account account : getServer().getAccounts()) {
			if (account.canEdit(getUri())) {
				editable = true;
			} else {
				copiable = true;
			}

			if (account.isSaving(getUri())) {
				saving = true;
			}
		}

		if (getExperience().getCanCopy() != null && !getExperience().getCanCopy()) {
			copiable = false;
		}

		final List<ScanEvent> history = getServer().getScanHistory(getUri());
		setVisible(binding.scanHistoryButton, history != null && !history.isEmpty() && (getExperience().getScanHistoryEnabled() == null || getExperience().getScanHistoryEnabled()));
		setVisible(binding.editButton, editable);
		setVisible(binding.copyButton, copiable);
		setVisible(binding.saveProgress, saving);
		setVisible(binding.buttonBar, !saving);
		return saving;
	}

	private Account getAccount() {
		for (Account account : getServer().getAccounts()) {
			if (account.canEdit(getUri())) {
				return account;
			}
		}
		return null;
	}

	private void setVisible(View view, boolean visible) {
		if (visible) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}
	}

	private void updateStarred() {
		getServer().loadStarred(new LoadCallback<List<String>>() {
			@Override
			public void loaded(List<String> item) {
				if (item.contains(getUri())) {
					binding.starButton.setText(R.string.unstar);
					binding.starButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_black_24dp, 0, 0);
				} else {
					binding.starButton.setText(R.string.star);
					binding.starButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_border_black_24dp, 0, 0);
				}
			}

			@Override
			public void error(Throwable e) {
				Analytics.trackException(e);
			}
		});
	}
}
