package com.icarus1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
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

        Fragment settingsFragment = getSupportFragmentManager().findFragmentById(R.id.settings_fragment_container);
        NavHostFragment settingsNavHostFragment = (NavHostFragment) settingsFragment;

        if (settingsNavHostFragment == null) {
            //TODO log
            finish();
            return;
        }

        nav = settingsNavHostFragment.getNavController();

        AppBarConfiguration appBarCfg = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(binding.settingsToolbar, nav, appBarCfg);

        setSupportActionBar(binding.settingsToolbar);

        // Navigate to second fragment that is effectively the starting fragment since back from
        // here closes the activity.
        nav.navigate(R.id.navigation_settings_action_start);

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

        NavDestination destination = nav.getCurrentDestination();

        if (destination == null) {
            //TODO log
            return;
        }

        int id = destination.getId();

        if (id == R.id.navigation_settings_fragment_settings) {
            finish();
        } else {
            nav.navigateUp();
        }

    }

}