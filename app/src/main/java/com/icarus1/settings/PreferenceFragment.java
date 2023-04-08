package com.icarus1.settings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.preference.PreferenceFragmentCompat;

import com.icarus1.R;
import com.icarus1.util.Debug;

public class PreferenceFragment extends PreferenceFragmentCompat {

    private static final MenuListener MENU_LISTENER = new MenuListener();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requireActivity().invalidateMenu();
        } catch (IllegalStateException e) {
            Debug.error("No activity attached.");
        }

    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().addMenuProvider(MENU_LISTENER);

    }

    @Override
    public void onPause() {

        requireActivity().removeMenuProvider(MENU_LISTENER);

        super.onPause();
    }

    private static class MenuListener implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        }
        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            menu.setGroupVisible(R.id.menu_group_core, false);
        }
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            return false;
        }
    }

}
