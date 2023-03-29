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
import com.icarus1.util.Debug;

public class SettingsActivity extends AppCompatActivity {

    private NavController nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

        Fragment settingsFragment = getSupportFragmentManager().findFragmentById(R.id.settings_fragment_container);

        if (settingsFragment == null) {
            Debug.error(new FragmentNotFoundException());
            finish();
            return;
        }

        NavHostFragment settingsNavHostFragment = (NavHostFragment) settingsFragment;

        nav = settingsNavHostFragment.getNavController();

        AppBarConfiguration appBarCfg = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(binding.settingsToolbar, nav, appBarCfg);

        setSupportActionBar(binding.settingsToolbar);

        // Navigate to second fragment that is effectively the starting fragment since back from
        // here closes the activity.
        NavDestination currentDestination = nav.getCurrentDestination();

        if (currentDestination == null) {
            Debug.error(new NavigationDestinationNotFound());
        } else if (currentDestination.getId() == R.id.navigation_settings_fragment_start) {
            nav.navigate(R.id.navigation_settings_action_start);
        }

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
            Debug.error(new NavigationDestinationNotFound());
            return;
        }

        int id = destination.getId();

        if (id == R.id.navigation_settings_fragment_settings) {
            finish();
        } else {
            nav.navigateUp();
        }

    }


    private static class FragmentNotFoundException extends Debug.Exception {
        private FragmentNotFoundException() {
            super("Fragment not found.");
        }
    }
    private static class NavigationDestinationNotFound extends Debug.Exception {
        private NavigationDestinationNotFound() {
            super("Navigation Destination null.");
        }
    }

}