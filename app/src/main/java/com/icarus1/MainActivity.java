package com.icarus1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.icarus1.databinding.ActivityMainBinding;
import com.icarus1.settings.SavedSettings;
import com.icarus1.util.Debug;

public class MainActivity extends AppCompatActivity {
    //TODO hide setting configuration behind splash screen

    private static final SavedSettings.NightModeListener NIGHT_MODE_LISTENER = new NightModeListener();

    private SavedSettings savedState;

    private ActivityMainBinding binding;
    private NavController nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentViewAndBinding();

        //
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

        //
        Fragment settingsFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

        if (settingsFragment == null) {
            Debug.error(new FragmentNotFoundException());
            finish();
            return;
        }

        NavHostFragment settingsNavHostFragment = (NavHostFragment) settingsFragment;

        nav = settingsNavHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, nav, appBarConfiguration);

        setSupportActionBar(binding.toolbar);

    }

    @Override
    protected void onDestroy() {

        savedState.destroy();

        super.onDestroy();
    }

    private void initContentViewAndBinding() {

        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {

        MenuItem item = menu.findItem(R.id.menu_item_dark_mode);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            item.setIcon(R.drawable.light_mode);
            item.setTitle(R.string.light_mode);
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            item.setIcon(R.drawable.dark_mode);
            item.setTitle(R.string.dark_mode);
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_UNDEFINED) {
            item.setIcon(R.drawable.dark_mode);
            item.setTitle(R.string.dark_mode);
        } else {
            Debug.log("Unrecognised nightModeFlags.");
        }

        NavDestination destination = nav.getCurrentDestination();
        if (destination != null) {
            if (destination.getId() == R.id.navigation_main_fragment_settings) {
                menu.setGroupVisible(R.id.menu_group_compass, false);
                menu.setGroupVisible(R.id.menu_group_all, false);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_item_settings) {
            nav.navigate(R.id.navigation_action_main_to_settings);
            return true;
        }

        if (id == R.id.menu_item_about) {
            //TODO switch to about fragment in MainActivity - requires rewrite of MainActivity
            return true;
        }

        if (id == R.id.menu_item_dark_mode) {
            toggleNightMode();
            return true;
        }

        if (id == android.R.id.home) {
            nav.navigateUp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class FragmentNotFoundException extends Debug.Exception {
        private FragmentNotFoundException() {
            super("Fragment Not Found.");
        }
    }


}