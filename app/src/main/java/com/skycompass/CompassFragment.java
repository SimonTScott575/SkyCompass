package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.util.OrientationSensor;
import com.skycompass.databinding.FragmentCompassBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Fn;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CompassFragment extends Fragment {

    private FragmentCompassBinding binding;
    private CompassFragmentViewModel viewModel;
    private SystemViewModel systemViewModel;

    private final MenuProvider menuProvider;
    private final OrientationSensor sensor;
    private Handler handler;
    private UpdateCompassRotation updateCompassRotation;

    public CompassFragment() {

        sensor = new OrientationSensor(new OnOrientationChanged());

        menuProvider = new MenuListener();
        updateCompassRotation = new UpdateCompassRotation();

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentCompassBinding.inflate(inflater);

        viewModel = new ViewModelProvider(this).get(CompassFragmentViewModel.class);
        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.compassView.setNorthRotation(viewModel.getTargetRotation());

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), location -> updateViews());
        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), date -> updateViews());
        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> updateViews());

    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel.getRotateToNorth() && !sensor.requested()) {
            sensor.request(requireContext());
        }

        requireActivity().addMenuProvider(menuProvider);

        startUpdateCompassRotation();

    }

    @Override
    public void onPause() {
        requireActivity().removeMenuProvider(menuProvider);
        sensor.destroy();
        endUpdateCompassRotation();
        super.onPause();
    }

    private void updateViews() {

        SystemViewModel.Location location = systemViewModel.getLocationLiveData().getValue();

        LocalDate date = systemViewModel.getDateLiveData().getValue();
        LocalTime time = systemViewModel.getTimeLiveData().getValue();

        binding.compassView.setLocation(location.latitude, location.longitude);
        binding.compassView.setDate(date);
        binding.compassView.setTime(
                ZonedDateTime.of(date, time, systemViewModel.getZoneOffset())
                        .withZoneSameInstant(ZoneOffset.ofHours(0))
                        .toLocalTime()
        );

    }

    public class OnOrientationChanged implements OrientationSensor.OnOrientationChanged {
        @Override
        public void onOrientationChanged(float[] orientation) {

            switch (requireContext().getDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    orientation[0] += Math.PI / 2d;
                    break;
                case Surface.ROTATION_180:
                    orientation[0] += 2 * Math.PI / 2d;
                    break;
                case Surface.ROTATION_270:
                    orientation[0] += 3 * Math.PI / 2d;
                    break;
            }

            orientation[0] = Fn.clampAngle(orientation[0]);
            setTargetRotation(orientation[0]);

        }
    }

    public void setRotateToNorth(boolean rotate) {

        viewModel.setRotateToNorth(rotate);

        if (rotate && !sensor.requested()) {
            sensor.request(requireContext());
            viewModel.setTargetRotation(0);
            binding.compassView.setNorthRotation(0);
        } else if (!rotate) {
            sensor.destroy();
            viewModel.setTargetRotation(0);
            binding.compassView.setNorthRotation(0);
        }

    }

    public void setTargetRotation(float rotation) {
        viewModel.setTargetRotation(rotation);
    }

    private class UpdateCompassRotation implements Runnable {

        private boolean end = false;

        @Override
        public void run() {

            if (viewModel.getRotateToNorth()) {

                float targetRotation = viewModel.getTargetRotation();
                float northRotation = binding.compassView.getNorthRotation();

                northRotation = Fn.lerpAngle(northRotation, targetRotation, 0.05f);

                binding.compassView.setNorthRotation(northRotation);

            }

            if (!end && !handler.postDelayed(this, 20))
                Debug.warn("Rotation handler failed to post.");

        }

        public void end() {
            end = true;
        }

    }

    private void startUpdateCompassRotation() {

        updateCompassRotation = new UpdateCompassRotation();

        handler = new Handler(requireActivity().getMainLooper());

        if (!handler.post(updateCompassRotation))
            Debug.warn("Rotation handler failed to post.");

    }

    private void endUpdateCompassRotation() {
        updateCompassRotation.end();
    }

    private class MenuListener implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {

            MenuItem item = menu.findItem(R.id.menu_item_compass);
            item.setVisible(true);
            item.setIcon(viewModel.isRotateToNorth() ? R.drawable.compass_off : R.drawable.compass);

        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            if (menuItem.getItemId() == R.id.menu_item_compass) {

                setRotateToNorth(!viewModel.isRotateToNorth());

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