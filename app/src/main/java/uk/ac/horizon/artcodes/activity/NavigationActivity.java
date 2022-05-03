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

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import uk.ac.horizon.artcodes.Analytics;
import uk.ac.horizon.artcodes.Features;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.NavigationBinding;
import uk.ac.horizon.artcodes.fragment.ExperienceLibraryFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceRecentFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceRecommendFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceSearchFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceStarFragment;
import uk.ac.horizon.artcodes.fragment.FeatureListFragment;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class NavigationActivity extends ArtcodeActivityBase implements
        NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAV_ITEM_ID = "navItemId";

    private final Handler drawerActionHandler = new Handler();
    private NavigationBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem selected;

    private static final String FRAGMENT_TAG = "fragment";
    private static final Handler searchHandler = new Handler();

    private GoogleApiClient apiClient;

    private final ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i("RESULT", "" + result);
                //if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();

                Log.i("RESULT", "" + data);
                if (data != null) {
                    final GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    Log.i("RESULT", "" + signInResult);
                    if (signInResult != null) {
                        Log.i("RESULT", "" + signInResult.getStatus());
                        Log.i("RESULT", "" + signInResult.getSignInAccount());
                        if (signInResult.getSignInAccount() != null) {
                            tryGoogleAccount(signInResult.getSignInAccount().getEmail(), signInResult.getSignInAccount().getDisplayName());
                        }
                    }
                }
                //}
            });

    private final ActivityResultLauncher<Intent> accountSelectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.hasExtra(AccountManager.KEY_ACCOUNT_NAME)) {
                            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                            if (accountName != null) {
                                tryGoogleAccount(accountName, null);
                            }
                        }
                    }
                }
            });


    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        if (menuItem.isCheckable()) {
            if (selected != null) {
                selected.setChecked(false);
            }

            selected = menuItem;
            menuItem.setChecked(true);
        }
        drawerActionHandler.postDelayed(() -> navigate(menuItem, true), DRAWER_CLOSE_DELAY_MS);

        binding.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.home) {
            return drawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAccounts() {
        final Menu menu = binding.navigation.getMenu();
        final MenuItem libraries = menu.findItem(R.id.nav_libraries);
        final SubMenu subMenu = libraries.getSubMenu();

        while (subMenu.size() > 0) {
            subMenu.removeItem(subMenu.getItem(0).getItemId());
        }

        final List<Account> accounts = getServer().getAccounts();
        for (int index = 0; index < accounts.size(); index++) {
            final Account account = accounts.get(index);
            final MenuItem menuItem = subMenu.add(R.id.navigation, index, Menu.NONE, account.getDisplayName());
            if (account.getId().equals("local")) {
                menuItem.setIcon(R.drawable.ic_folder_24dp);
            } else {
                menuItem.setIcon(R.drawable.ic_cloud_24dp);
            }
            menuItem.setCheckable(true);
        }

        final MenuItem menuItem = subMenu.add(R.id.navigation, R.id.nav_addaccount, Menu.NONE, R.string.nav_addaccount);
        menuItem.setIcon(R.drawable.ic_add_24dp);

        for (int i = 0, count = binding.navigation.getChildCount(); i < count; i++) {
            final View child = binding.navigation.getChildAt(i);
            if (child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }

        getServer().loadRecent(new LoadCallback<List<String>>() {
            @Override
            public void loaded(List<String> item) {
                final MenuItem recent = menu.findItem(R.id.nav_recent);
                if (recent != null) {
                    recent.setVisible(!item.isEmpty());
                }
            }

            @Override
            public void error(Throwable e) {
                Analytics.trackException(e);
            }
        });
        getServer().loadStarred(new LoadCallback<List<String>>() {
            @Override
            public void loaded(List<String> item) {
                final MenuItem starred = menu.findItem(R.id.nav_starred);
                if (starred != null) {
                    starred.setVisible(!item.isEmpty());
                }
            }

            @Override
            public void error(Throwable e) {
                Analytics.trackException(e);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("a", "New intent " + intent);
        super.onNewIntent(intent);
    }

    public void navigate(Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment, FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Runnable delayedAction = null;

            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (delayedAction != null) {
                    searchHandler.removeCallbacks(delayedAction);
                    delayedAction = null;
                }

                if (newText.trim().length() > 3) {
                    delayedAction = () -> search(newText);

                    searchHandler.postDelayed(delayedAction, 1000);
                }
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(final MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(final MenuItem item) {
                Log.i("a", "Closed");
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                if (fragment instanceof ExperienceSearchFragment) {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            }
        });

        return true;
    }

    private void search(String query) {
        Analytics.logSearch(query);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof ExperienceSearchFragment) {
            ((ExperienceSearchFragment) fragment).search(query);
        } else {
            ExperienceSearchFragment experienceSearchFragment = new ExperienceSearchFragment();
            experienceSearchFragment.setArguments(new Bundle());
            experienceSearchFragment.getArguments().putString("query", query);
            navigate(experienceSearchFragment, true);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Features.show_welcome.isEnabled(this)) {
            startActivity(new Intent(this, AboutArtcodesActivity.class));
            return;
        }

        binding = DataBindingUtil.setContentView(this, R.layout.navigation);

        setSupportActionBar(binding.toolbar);

        binding.navigation.setNavigationItemSelectedListener(this);

        final View headerView = binding.navigation.inflateHeaderView(R.layout.navigation_header);

        final MenuItem featureItem = binding.navigation.getMenu().findItem(R.id.nav_features);
        if (Features.edit_features.isEnabled(this)) {
            featureItem.setVisible(true);
        } else {
            headerView.setLongClickable(true);
            headerView.setOnLongClickListener(v -> {
                featureItem.setVisible(true);
                Features.edit_features.setEnabled(this, true);
                return false;
            });
        }

        updateAccounts();

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateAccounts();
            }
        };
        binding.drawer.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

        int navigationIndex = R.id.nav_home;
        if (savedInstanceState != null) {
            navigationIndex = savedInstanceState.getInt(NAV_ITEM_ID, R.id.nav_home);
        }
        MenuItem item = binding.navigation.getMenu().findItem(navigationIndex);
        if (item == null) {
            item = binding.navigation.getMenu().findItem(R.id.nav_home);
        }

        navigate(item, false);

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> Log.i("Signin", "Failed " + connectionResult))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        apiClient.connect();

    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selected != null) {
            outState.putInt(NAV_ITEM_ID, selected.getItemId());
        }
    }

    private void tryGoogleAccount(final String name, final String displayName) {
        new Thread(() -> {
            try {
                final Account account = getServer().createAccount("google:" + name);
                if (account.validates()) {
                    account.setDisplayName(displayName);
                    getServer().add(account);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        updateAccounts();
                        final Bundle bundle = new Bundle();
                        bundle.putString("account", account.getId());
                        final Fragment fragment = new ExperienceLibraryFragment();
                        fragment.setArguments(bundle);
                        navigate(fragment, true);
                    });
                }
            } catch (UserRecoverableAuthException userRecoverableException) {
                Log.i("intent", "Intent = " + userRecoverableException.getIntent());
                accountSelectLauncher.launch(userRecoverableException.getIntent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void selectAccount() {
        apiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        loginLauncher.launch(signInIntent);
    }

    @SuppressLint("NonConstantResourceId")
    private void navigate(MenuItem item, boolean addToBackStack) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                navigate(new ExperienceRecommendFragment(), addToBackStack);
                break;

            case R.id.nav_starred:
                navigate(new ExperienceStarFragment(), addToBackStack);
                break;

            case R.id.nav_features:
                navigate(new FeatureListFragment(), addToBackStack);
                break;

            case R.id.nav_recent:
                navigate(new ExperienceRecentFragment(), addToBackStack);
                break;

            case R.id.nav_about_artcodes:
                startActivity(new Intent(this, AboutArtcodesActivity.class));
                break;

            case R.id.nav_addaccount:
                selectAccount();
                break;

            default:
                final List<Account> accounts = getServer().getAccounts();
                if (item.getItemId() < accounts.size()) {
                    final Account account = accounts.get(item.getItemId());
                    Bundle bundle = new Bundle();
                    bundle.putString("account", account.getId());
                    final Fragment fragment = new ExperienceLibraryFragment();
                    fragment.setArguments(bundle);
                    navigate(fragment, addToBackStack);
                }
                break;
        }
    }
}
