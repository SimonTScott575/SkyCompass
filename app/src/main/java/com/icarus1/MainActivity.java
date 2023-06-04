package com.icarus1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.icarus1.databinding.ActivityMainBinding;
import com.icarus1.settings.SavedSettings;
import com.icarus1.util.Debug;

public class MainActivity extends AppCompatActivity {
    //TODO hide setting configuration behind splash screen

    private static final SavedSettings.NightModeListener NIGHT_MODE_LISTENER = new NightModeListener();

    private SavedSettings savedState;

    private ActivityMainBinding binding;
    private NavController navigationController;
    private final MenuListener menuListener = new MenuListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBindingAndContentView();
        initToolbarAndActionBar();

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

        if (mainFragmentContainer == null) {
            Debug.error(new FragmentNotFoundException());
            finish();
            return;
        }

        NavHostFragment mainNavHostFragment = (NavHostFragment) mainFragmentContainer;

        navigationController = mainNavHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navigationController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navigationController, appBarConfiguration);
        setSupportActionBar(binding.toolbar);

    }

    private class MenuListener implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {

            menu.setGroupVisible(R.id.menu_group_core, true);

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
                try {
                    if(navigationController.getCurrentDestination().getId() == R.id.navigation_main_fragment_main) {
                        navigationController.navigate(R.id.navigation_action_main_to_settings);
                    } else if(navigationController.getCurrentDestination().getId() == R.id.navigation_main_fragment_help) {
                        navigationController.navigate(R.id.navigation_action_help_to_settings);
                    }
                } catch (NullPointerException e){
                    Debug.log(e.getMessage());
                }
                return true;
            }

            if (id == R.id.menu_item_help) {
                navigationController.navigate(R.id.navigation_action_main_to_help);
                return true;
            }

            if (id == R.id.menu_item_dark_mode) {
                toggleNightMode();
                return true;
            }

            if (id == android.R.id.home) {
                navigationController.navigateUp();
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

    private static class FragmentNotFoundException extends Debug.Exception {
        private FragmentNotFoundException() {
            super("Fragment Not Found.");
        }
    }


}