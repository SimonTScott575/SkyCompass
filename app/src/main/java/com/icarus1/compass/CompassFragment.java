package com.icarus1.compass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.R;
import com.icarus1.databinding.FragmentCompassBinding;
import com.icarus1.util.Debug;

public class CompassFragment extends Fragment {

    private final CompassModel compassModel;

    private FragmentCompassBinding binding;
    private CompassFragmentViewModel viewModel;

    public CompassFragment() {

        compassModel = new CompassModel(0, 0);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.menu_item_rotate_to_north) {

                    boolean rotateToNorth = !viewModel.isRotateToNorth();

                    binding.compassView.setRotateToNorth(rotateToNorth);
                    viewModel.setRotateToNorth(rotateToNorth);

                    if (viewModel.isRotateToNorth()) {
                        menuItem.setIcon(R.drawable.compass_off);
                    } else {
                        menuItem.setIcon(R.drawable.compass);
                    }

                    return true;
                }

                return false;
            }
        });

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentCompassBinding.inflate(inflater);
        viewModel = new ViewModelProvider(this).get(CompassFragmentViewModel.class);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.compassView.setCompassModel(compassModel);
        binding.compassView.setRotateToNorth(viewModel.isRotateToNorth());
        binding.compassView.setNorthRotation(viewModel.getNorthRotation(), true);

    }

    public void setNorthRotation(float rotation) {
        binding.compassView.setNorthRotation(rotation);
        viewModel.setNorthRotation(rotation);
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
    public void setDrawBody(CelestialBody body, boolean draw) {
        binding.compassView.setDrawBody(body, draw);
    }

    public boolean getDrawBody(CelestialBody body) {
        return binding.compassView.getDrawBody(body);
    }

}