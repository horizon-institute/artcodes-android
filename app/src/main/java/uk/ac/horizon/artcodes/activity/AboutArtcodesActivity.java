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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.Features;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.AboutBinding;
import uk.ac.horizon.artcodes.fragment.AboutFragment;

public class AboutArtcodesActivity extends ArtcodeActivityBase {
	public static class AboutPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> fragments = new ArrayList<>();

		AboutPagerAdapter(FragmentManager fm) {
			super(fm);

			fragments.add(AboutFragment.create(R.layout.about_artcodes));
			fragments.add(AboutFragment.create(R.layout.about_artcodes_2));
			fragments.add(AboutFragment.create(R.layout.about_artcodes_3));
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Generate title based on item position
			return null;
		}
	}

	private AboutBinding binding;

	public void finish(View view) {
		startActivity(new Intent(this, NavigationActivity.class));
		Features.show_welcome.setEnabled(this, false);
	}

	public void nextPage(View view) {
		binding.viewpager.setCurrentItem(binding.viewpager.getCurrentItem() + 1);
	}

	public void more(View view) {
		startActivity(new Intent(this, AboutDrawingActivity.class));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.about);
		binding.viewpager.setAdapter(new AboutPagerAdapter(getSupportFragmentManager()));
		binding.indicator.setViewPager(binding.viewpager);
		binding.indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if (position == (binding.viewpager.getAdapter().getCount() - 1)) {
					//binding.moreButton.setVisibility(View.VISIBLE);
					binding.doneButton.setVisibility(View.VISIBLE);
					binding.nextButton.setVisibility(View.GONE);
					binding.skipButton.setVisibility(View.INVISIBLE);
				} else {
					//binding.moreButton.setVisibility(View.GONE);
					binding.doneButton.setVisibility(View.GONE);
					binding.nextButton.setVisibility(View.VISIBLE);
					binding.skipButton.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}
}
