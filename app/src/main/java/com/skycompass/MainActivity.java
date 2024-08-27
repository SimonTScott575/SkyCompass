package com.skycompass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.skycompass.databinding.ActivityMainBinding;
import com.skycompass.util.Debug;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences preferences;

    private ActivityMainBinding binding;

    private NavController navigationController;

    private final MenuListener menuListener = new MenuListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.registerOnSharedPreferenceChangeListener(this);

        //
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        boolean requiresUpdate = false;

        String savedNightMode = preferences.getString("NightMode", "");

        switch (savedNightMode) {
            case "DAY" :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                requiresUpdate = (currentNightMode & Configuration.UI_MODE_NIGHT_YES) != 0;
                break;
            case "NIGHT" :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                requiresUpdate = (currentNightMode & Configuration.UI_MODE_NIGHT_NO) != 0;
                break;
            default :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        if (requiresUpdate)
            return;

        // Inflate views
        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);

        setContentView(binding.getRoot());

        // Create fragment
        Fragment mainFragmentContainer = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

        if (mainFragmentContainer == null)
            throw new RuntimeException("Main fragment not found.");

        // Initialise app bar and navigation
        NavHostFragment mainNavHostFragment = (NavHostFragment) mainFragmentContainer;

        navigationController = mainNavHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navigationController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navigationController, appBarConfiguration);
        setSupportActionBar(binding.toolbar);

        addMenuProvider(menuListener);

    }

    @Override
    protected void onDestroy() {

        preferences.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    private class MenuListener implements MenuProvider {

        private int currentDestination;

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {

            NavDestination dest = navigationController.getCurrentDestination();

            if (dest != null)
                currentDestination = dest.getId();

            menu.setGroupVisible(R.id.menu_group_core, true);
            menu.findItem(R.id.menu_item_dark_mode).setVisible(currentDestination == R.id.navigation_main_fragment_main);
            menu.findItem(R.id.menu_item_help).setVisible(currentDestination == R.id.navigation_main_fragment_main);
            menu.findItem(R.id.menu_item_settings).setVisible(currentDestination == R.id.navigation_main_fragment_main);

            MenuItem darkModeItem = menu.findItem(R.id.menu_item_dark_mode);

            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    darkModeItem.setIcon(R.drawable.light_mode);
                    darkModeItem.setTitle(R.string.light_mode);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    darkModeItem.setIcon(R.drawable.dark_mode);
                    darkModeItem.setTitle(R.string.dark_mode);
                    break;
                default:
                    Debug.warn("Night mode not recognised.");
                    darkModeItem.setIcon(R.drawable.dark_mode);
                    darkModeItem.setTitle(R.string.dark_mode);
            }

        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            int id = menuItem.getItemId();

            if (id == R.id.menu_item_settings) {

                if(currentDestination == R.id.navigation_main_fragment_main) {
                    navigationController.navigate(R.id.navigation_action_main_to_settings);
                    invalidateMenu();
                }

                return true;

            } else if (id == R.id.menu_item_help) {

                if (currentDestination == R.id.navigation_main_fragment_main) {
                    navigationController.navigate(R.id.navigation_action_main_to_help);
                    invalidateMenu();
                }

                return true;

            } else if (id == R.id.menu_item_dark_mode) {

                toggleNightMode();

                return true;

            } else if (id == android.R.id.home) {

                navigationController.navigateUp();
                invalidateMenu();

                return true;

            }

            return false;
        }

    }

    private void toggleNightMode() {

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                preferences.edit()
                        .putString("NightMode", "DAY")
                        .apply();
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                preferences.edit()
                        .putString("NightMode", "NIGHT")
                        .apply();
                break;
            default:
                Debug.warn("Night mode not recognised.");
                preferences.edit()
                        .putString("NightMode", "DAY")
                        .apply();
                break;
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {

        if (s == null || !s.equals("NightMode"))
            return;

        switch (sharedPreferences.getString("NightMode", "")) {
            case "DAY" :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "NIGHT" :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

    }

}