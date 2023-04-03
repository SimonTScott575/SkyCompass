package com.icarus1;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SavedState {
    //TODO apply problematic as implemented in listeners of MainActivity

    private static final String NIGHT_MODE_KEY = "night_mode_preference";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private NightModeListener nightModeListener;

    private SavedState(Context context) {

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        onSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener();

        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

    }

    public void destroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static SavedState from(Context context) {
        return new SavedState(context);
    }

    public NightMode getNightMode() {
        String nightMode = preferences.getString(NIGHT_MODE_KEY, NightMode.SYSTEM.name());
        if (nightMode.equals("DAY")) {
            return NightMode.DAY;
        } else if (nightMode.equals("NIGHT")) {
            return NightMode.NIGHT;
        } else {
            return NightMode.SYSTEM;
        }
    }

    public void stageNightMode(NightMode nightMode) {
        editor.putString(NIGHT_MODE_KEY, nightMode.name());
    }

    public void setNightModeListener(NightModeListener nightModeListener) {
        this.nightModeListener = nightModeListener;
    }

    public void apply() {
        editor.apply();
    }

    private class OnSharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals(NIGHT_MODE_KEY)) {
                if (nightModeListener != null) {
                    nightModeListener.onNightModeChanged(getNightMode());
                }
            }

        }
    }

    enum NightMode {
        DAY,
        NIGHT,
        SYSTEM
    }

    public interface NightModeListener {
        void onNightModeChanged(NightMode nightMode);
    }

}
