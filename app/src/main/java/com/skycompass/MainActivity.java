package com.skycompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.skycompass.databinding.ActivityMainBinding;
import com.skycompass.settings.SavedSettings;
import com.skycompass.util.Debug;

public class MainActivity extends AppCompatActivity {

    private static final SavedSettings.NightModeListener NIGHT_MODE_LISTENER = new NightModeListener();

    private SavedSettings savedState;

    private ActivityMainBinding binding;
    private NavController navigationController;
    private final MenuListener menuListener = new MenuListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SavedSettings.NightMode currentNightMode;
        int currentNightModeCode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightModeCode == Configuration.UI_MODE_NIGHT_YES) {
            currentNightMode = SavedSettings.NightMode.NIGHT;
        } else if (currentNightModeCode == Configuration.UI_MODE_NIGHT_NO) {
            currentNightMode = SavedSettings.NightMode.DAY;
        } else {
            currentNightMode = null;
        }

        savedState = SavedSettings.from(this);
        savedState.setNightModeListener(NIGHT_MODE_LISTENER);

        SavedSettings.NightMode savedNightMode = savedState.getNightMode();
        switch (savedNightMode) {
            case DAY :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case NIGHT :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        if (
            savedNightMode != SavedSettings.NightMode.SYSTEM &&
            currentNightMode != null &&
            currentNightMode != savedNightMode
        ) {
            return;
        }

        initBindingAndContentView();
        initToolbarAndActionBar();

        addMenuProvider(menuListener);

    }

    @Override
    protected void onDestroy() {

        savedState.destroy();

        super.onDestroy();
    }

    private void initBindingAndContentView() {

        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

    }

    private void initToolbarAndActionBar() {

        Fragment mainFragmentContainer = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

        if (mainFragmentContainer == null)
            throw new RuntimeException("Main fragment not found.");

        NavHostFragment mainNavHostFragment = (NavHostFragment) mainFragmentContainer;

        navigationController = mainNavHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navigationController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navigationController, appBarConfiguration);
        setSupportActionBar(binding.toolbar);

    }

    private class MenuListener implements MenuProvider {

        private int currentDestination;

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {

            try {
                NavDestination dest = navigationController.getCurrentDestination();
                if (dest != null) {
                    currentDestination = dest.getId();
                }
            } catch (NullPointerException e){
                Debug.error(e.getMessage());
            }

            menu.setGroupVisible(R.id.menu_group_core, true);
            menu.findItem(R.id.menu_item_dark_mode).setVisible(currentDestination == R.id.navigation_main_fragment_main);
            menu.findItem(R.id.menu_item_help).setVisible(currentDestination == R.id.navigation_main_fragment_main);
            menu.findItem(R.id.menu_item_settings).setVisible(currentDestination == R.id.navigation_main_fragment_main);

            MenuItem darkModeItem = menu.findItem(R.id.menu_item_dark_mode);
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                darkModeItem.setIcon(R.drawable.light_mode);
                darkModeItem.setTitle(R.string.light_mode);
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
                darkModeItem.setIcon(R.drawable.dark_mode);
                darkModeItem.setTitle(R.string.dark_mode);
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_UNDEFINED) {
                darkModeItem.setIcon(R.drawable.dark_mode);
                darkModeItem.setTitle(R.string.dark_mode);
            } else {
                Debug.log("Unrecognised nightModeFlags.");
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
            }

            if (id == R.id.menu_item_help) {
                if (currentDestination == R.id.navigation_main_fragment_main) {
                    navigationController.navigate(R.id.navigation_action_main_to_help);
                    invalidateMenu();
                }
                return true;
            }

            if (id == R.id.menu_item_dark_mode) {
                toggleNightMode();
                return true;
            }

            if (id == android.R.id.home) {
                navigationController.navigateUp();
                invalidateMenu();
                return true;
            }

            return false;
        }

    }

    private void toggleNightMode() {

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            savedState.stageNightMode(SavedSettings.NightMode.DAY);
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            savedState.stageNightMode(SavedSettings.NightMode.NIGHT);
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
            savedState.stageNightMode(SavedSettings.NightMode.DAY);
        }

        savedState.apply();

    }

    private static class NightModeListener implements SavedSettings.NightModeListener {
        @Override
        public void onNightModeChanged(SavedSettings.NightMode nightMode) {

            switch (nightMode) {
                case DAY :
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case NIGHT :
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default :
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }

        }
    }

}