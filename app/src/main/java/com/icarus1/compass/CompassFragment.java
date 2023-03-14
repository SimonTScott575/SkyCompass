package com.icarus1.compass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.databinding.FragmentCompassBinding;

public class CompassFragment extends Fragment {

    private final CompassModel compassModel;

    private FragmentCompassBinding binding;

    public CompassFragment() {

        compassModel = new CompassModel(0, 0);

    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentCompassBinding.inflate(inflater);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.compassView.setCompassModel(compassModel);

    }

    public void setLocation(double longitude, double latitude) {

        compassModel.setLongitude(longitude);
        compassModel.setLatitude(latitude);
        binding.compassView.invalidate();

    }

    public void setDate(int year, int month, int dayOfMonth) {

        compassModel.setDate(year, month, dayOfMonth);
        binding.compassView.invalidate();

    }

    public void setTime(int hour, int minute, float seconds) {

        binding.compassView.setTime(hour, minute, seconds);

    }

    public void setDrawSunMoon(boolean drawSunMoon) {
        binding.compassView.setDrawSunMoon(drawSunMoon);
    }

    public boolean isDrawSunMoon() {
        return binding.compassView.isDrawSunMoon();
    }

    public void setDrawPlanets(boolean drawPlanets) {
        binding.compassView.setDrawPlanets(drawPlanets);
    }

    public boolean isDrawPlanets() {
        return binding.compassView.isDrawPlanets();
    }

}