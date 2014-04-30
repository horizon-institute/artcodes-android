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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.MarkerPreferences;
import uk.ac.horizon.data.DtouchMarkersDataSource;
import uk.ac.horizon.dtouchMobile.MarkerConstraint;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = SettingsActivity.class.getName();
	private MarkerPreferences markerPreferences;

	/**
	 * Sets up the action bar for an {@link PreferenceScreen}
	 */
	private static void initializeActionBar(PreferenceScreen preferenceScreen)
	{
		final Dialog dialog = preferenceScreen.getDialog();

		if (dialog != null)
		{
			// Inialize the action bar
			dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

			// Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
			// events instead of passing to the activity
			// Related Issue: https://code.google.com/p/android/issues/detail?id=4611
			View homeBtn = dialog.findViewById(android.R.id.home);

			if (homeBtn != null)
			{
				View.OnClickListener dismissDialogClickListener = new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
					}
				};

				// Prepare yourselves for some hacky programming
				ViewParent homeBtnContainer = homeBtn.getParent();

				// The home button is an ImageView inside a FrameLayout
				if (homeBtnContainer instanceof FrameLayout)
				{
					ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

					if (containerParent instanceof LinearLayout)
					{
						// This view also contains the title text, set the whole view as clickable
						((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
					}
					else
					{
						// Just set it on the home button
						((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
					}
				}
				else
				{
					// The 'If all else fails' default case
					homeBtn.setOnClickListener(dismissDialogClickListener);
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		markerPreferences = new MarkerPreferences(this);

		addPreferencesFromResource(R.xml.settings_marker);

		PreferenceCategory category = (PreferenceCategory) findPreference("markerURLs");

		Preference button = findPreference("add_marker");
		assert button != null;
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				DialogFragment newFragment = new MarkerDialogFragment();
				newFragment.show(getFragmentManager(), "New Marker");
				return true;
			}
		});

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		assert sharedPreferences != null;
		assert sharedPreferences.getAll() != null;
		for (String key : sharedPreferences.getAll().keySet())
		{
			Log.i(TAG, key);

			if (key.startsWith("code_"))
			{
				final String url = sharedPreferences.getString(key, null);
				final EditTextPreference markerSetting = createMarkerSetting(key, url);
				category.addPreference(markerSetting);
			}
			else
			{
				final Preference preference = findPreference(key);
				if (preference != null)
				{
					preference.setSummary(sharedPreferences.getString(key, null));
				}
			}
		}
	}

	private EditTextPreference createMarkerSetting(final String key, final String url)
	{
		final EditTextPreference markerSetting = new EditTextPreference(this);
		final String code = key.substring("code_".length()).replaceAll("_", ":");
		markerSetting.setTitle("Marker " + code);

		markerSetting.setSummary(url);
		markerSetting.setText(url);
		markerSetting.setKey(key);
		markerSetting.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_CLASS_TEXT);
		markerSetting.setOrder(Preference.DEFAULT_ORDER);
		markerSetting.setDialogTitle("URL for Marker " + code);

		return markerSetting;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		DtouchMarkersDataSource.prefsChanged = true;
		Log.i(TAG, key + " changed");
		final Preference preference = findPreference(key);
		if (preference != null)
		{
			if (key.startsWith("code_") && (sharedPreferences.getString(key, null) == null || sharedPreferences.getString(key, null).isEmpty()))
			{
				sharedPreferences.edit().remove(key).commit();
				PreferenceCategory category = (PreferenceCategory) findPreference("markerURLs");
				assert category != null;
				category.removePreference(preference);
			}
			else
			{
				preference.setSummary(sharedPreferences.getString(key, null));
			}
		}
	}

	private void resetPrefs()
	{
		new AlertDialog.Builder(this).setTitle("Reset MarkerPreferences?").setMessage("Are you sure you want to reset all preferences to their default value?")
				.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Reset", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int whichButton)
			{
				resetToDefault();
			}
		}).setNegativeButton("Cancel", null).show();

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		super.onPreferenceTreeClick(preferenceScreen, preference);

		// If the user has clicked on a preference screen, set up the action bar
		if (preference instanceof PreferenceScreen)
		{
			initializeActionBar((PreferenceScreen) preference);
		}

		return false;
	}

	// Clear the prefs and reset to default
	private void resetToDefault()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().clear().commit();
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_marker, true);
	}

	private class MarkerDialogFragment extends DialogFragment
	{
		private EditText markerCode;
		private EditText markerURL;
		private AlertDialog alertDialog;

		private void checkValid()
		{
			Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			if (button != null)
			{
				button.setEnabled(false);
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
			final LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			@SuppressLint("InflateParams")
			final View view = inflater.inflate(R.layout.dialog_marker, null);

			assert view != null;
			markerCode = (EditText) view.findViewById(R.id.markerCode);
			markerCode.setFilters(new InputFilter[]{new MarkerCodeInputFilter()});
			markerURL = (EditText) view.findViewById(R.id.markerURL);

			builder.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.dialog_action_create, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int id)
						{
							SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
							String key = "code_" + markerCode.getText().toString().replaceAll(":", "_");
							Log.i(TAG, key + " = " + markerURL.getText().toString());
							preferences.edit().putString(key, markerURL.getText().toString()).commit();
							MarkerDialogFragment.this.getDialog().dismiss();

							PreferenceCategory category = (PreferenceCategory) findPreference("markerURLs");
							assert category != null;
							final EditTextPreference markerSetting = createMarkerSetting(key, markerURL.getText().toString());
							category.addPreference(markerSetting);

						}
					})
					.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							MarkerDialogFragment.this.getDialog().cancel();
						}
					});

			alertDialog = builder.create();
			checkValid();
			return alertDialog;
		}

		private class MarkerCodeInputFilter implements InputFilter
		{
			private final MarkerConstraint constraints;

			public MarkerCodeInputFilter()
			{
				constraints = new MarkerConstraint(markerPreferences);
			}

			@Override
			public CharSequence filter(CharSequence source, int sourceStart, int sourceEnd, Spanned destination, int destinationStart, int destinationEnd)
			{
				String sourceValue = source.subSequence(sourceStart, sourceEnd).toString();
				if (sourceValue.equals(" "))
				{
					sourceValue = ":";
				}

				boolean result = constraints.validateMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
						destination.subSequence(destinationEnd, destination.length()).toString(), true);

				if (!result && !sourceValue.startsWith(":"))
				{
					sourceValue = ":" + sourceValue;
					result = constraints.validateMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
							destination.subSequence(destinationEnd, destination.length()).toString(), true);
				}

				if (result && !source.subSequence(sourceStart, sourceEnd).toString().equals(sourceValue))
				{
					return sourceValue;
				}

				if (result)
				{
					return null;
				}
				return "";
			}
		}
	}
}
