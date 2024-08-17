package com.skycompass;

import android.content.res.Configuration;
import android.graphics.Color;
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.compassView.setNorthRotation(viewModel.getTargetRotation());

        int nightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.compassView.setColor(Color.valueOf(Color.parseColor("#F55353")));
        }

        Bundle args = getArguments();
        if (args != null) {

            // Location
            double longitude = args.getDouble("Longitude");
            double latitude = args.getDouble("Latitude");

            setLocation(longitude, latitude);

            // Date
            LocalDate date = LocalDate.of(args.getInt("Y"), args.getInt("M")+1, args.getInt("D")+1);

            setDate(date);

            // Time
            Comms.Time time = Comms.Time.from(args);
            setTime(
                ZonedDateTime.of(date, time.getTime(), time.getZoneOffset())
                    .withZoneSameInstant(ZoneOffset.ofHours(0))
                    .toLocalTime()
            );

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel.getRotateToNorth() && !sensor.requested()) {
            sensor.request(requireContext());
        }

        requireActivity().addMenuProvider(menuProvider);

        try {
            startUpdateCompassRotation();
        } catch (HandlerNoPostException e) {
            Debug.error(e);
        }

    }

    @Override
    public void onPause() {
        requireActivity().removeMenuProvider(menuProvider);
        sensor.destroy();
        endUpdateCompassRotation();
        super.onPause();
    }

    public void setLocation(double longitude, double latitude) {
        binding.compassView.setLocation(latitude, longitude);
    }

    public void setDate(LocalDate date) {
        binding.compassView.setDate(date);
    }

    public void setTime(LocalTime time) {
        binding.compassView.setTime(time);
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

            if (!end) {
                boolean success = handler.postDelayed(this, 20);
                if (!success) {
                    Debug.error(new HandlerNoPostException());
                }
            }

        }

        public void end() {
            end = true;
        }

    }

    private void startUpdateCompassRotation()
    throws HandlerNoPostException {

        updateCompassRotation = new UpdateCompassRotation();
        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(updateCompassRotation);
        if (!success) {
            throw new HandlerNoPostException();
        }

    }

    private void endUpdateCompassRotation() {
        updateCompassRotation.end();
    }

    private static class HandlerNoPostException extends Debug.Exception {
        public HandlerNoPostException() {
            super("Handler failed to post.");
        }
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