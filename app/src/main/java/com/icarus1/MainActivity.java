package com.icarus1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import com.icarus1.databinding.ActivityMainBinding;
import com.icarus1.settings.SavedSettings;
import com.icarus1.util.Debug;

public class MainActivity extends AppCompatActivity {
    //TODO hide setting configuration behind splash screen

    private static final SavedSettings.NightModeListener NIGHT_MODE_LISTENER = new NightModeListener();

    private SavedSettings savedState;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentViewAndBinding();
        initToolbar();

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

    private void initToolbar() {

        binding.toolbar.setOnMenuItemClickListener(new OnMenuClick());
        binding.toolbar.inflateMenu(R.menu.main);

        MenuItem item = binding.toolbar.getMenu().findItem(R.id.menu_dark_mode);
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

    private class OnMenuClick implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (item.getItemId() == R.id.menu_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            if (item.getItemId() == R.id.menu_about) {
                //TODO switch to about fragment in MainActivity - requires rewrite of MainActivity
                return true;
            }

            if (item.getItemId() == R.id.menu_dark_mode) {
                toggleNightMode();
                return true;
            }

            return false;
        }
    }

}