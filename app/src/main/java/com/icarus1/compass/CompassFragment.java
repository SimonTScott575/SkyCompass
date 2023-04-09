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

public class CompassFragment extends Fragment {

    private final CompassModel compassModel;

    private FragmentCompassBinding binding;
    private CompassFragmentViewModel viewModel;

    private final MenuProvider menuProvider;

    public CompassFragment() {
        compassModel = new CompassModel(0, 0);
        menuProvider = new MenuListener();
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
        binding.compassView.setNorthRotation(viewModel.getNorthRotation());
        binding.compassView.setCurrentRotation(viewModel.getNorthRotation());

    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().addMenuProvider(menuProvider);

    }

    @Override
    public void onPause() {

        requireActivity().removeMenuProvider(menuProvider);

        super.onPause();
    }

    public void setNorthRotation(float rotation) {
        binding.compassView.setNorthRotation(rotation);
        viewModel.setNorthRotation(rotation);
    }

    public void setLocation(double longitude, double latitude) {
        compassModel.setLocation(latitude, longitude);
        binding.compassView.invalidate();
    }

    public void setDate(int year, int month, int dayOfMonth) {
        compassModel.setDate(year, month, dayOfMonth);
        binding.compassView.invalidate();
    }

    public void setTime(int hour, int minute, float seconds) {
        binding.compassView.setTime(hour, minute, seconds);
    }

    public void setDrawBody(CelestialObject body, boolean draw) {
        binding.compassView.setDrawBody(body, draw);
    }

    public boolean getDrawBody(CelestialObject body) {
        return binding.compassView.getDrawBody(body);
    }

    private class MenuListener implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {

            MenuItem item = menu.findItem(R.id.menu_item_rotate_to_north);

            item.setVisible(true);
            item.setIcon(viewModel.isRotateToNorth() ? R.drawable.compass_off : R.drawable.compass);

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

    }

}