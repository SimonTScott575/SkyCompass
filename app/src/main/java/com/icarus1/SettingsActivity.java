package com.icarus1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

import com.icarus1.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private NavController nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

        NavHostFragment settingsFragmentHost =
            (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.settings_fragment_container);
        nav = settingsFragmentHost.getNavController();
        AppBarConfiguration appBarCfg = new AppBarConfiguration.Builder(nav.getGraph()).build();

        // Navigate to second fragment that is effectively the starting fragment since back from
        // here closes the activity.
        nav.navigate(R.id.navigation_settings_action_start);

        NavigationUI.setupWithNavController(binding.settingsToolbar, nav, appBarCfg);

        setSupportActionBar(binding.settingsToolbar);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationUp();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        navigationUp();
    }

    private void navigationUp() {

        if (nav.getCurrentDestination().getId() == R.id.navigation_settings_fragment_settings) {
            finish();
        } else {
            nav.navigateUp();
        }

    }

}