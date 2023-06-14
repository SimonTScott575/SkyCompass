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
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.compass.CompassModel;
import com.skycompass.compass.CompassSensor;
import com.skycompass.databinding.FragmentCompassBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.TimeZone;

public class CompassFragment extends Fragment {

    private FragmentCompassBinding binding;
    private CompassFragmentViewModel viewModel;

    private final CompassModel compassModel;
    private final MenuProvider menuProvider;
    private final CompassSensor sensor;
    private Handler handler;
    private UpdateCompassRotation updateCompassRotation;

    public CompassFragment() {
        compassModel = new CompassModel(0, 0);
        sensor = new CompassSensor(orientation -> setTargetRotation(orientation[0]));
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

        binding.compassView.setCompassModel(compassModel);
        binding.compassView.setNorthRotation(viewModel.getTargetRotation());

        int nightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.compassView.setColor(Color.valueOf(Color.parseColor("#FF0000")));
        }

        Bundle args = getArguments();
        if (args != null) {

            double longitude = args.getDouble("Longitude");
            double latitude = args.getDouble("Latitude");
            setLocation(longitude, latitude);

            int year = args.getInt("Y");
            int month = args.getInt("M");
            int day = args.getInt("D");
            setDate(year, month, day);

            int UTCOffset = args.getInt("OFFSET");
            TimeZone timeZone = new TimeZone(UTCOffset);
            int hour = args.getInt("HOUR") - timeZone.getRawHourOffset();
            int minute = args.getInt("MINUTE") - timeZone.getRawMinuteOffset();
            float seconds = args.getInt("SECOND") - timeZone.getRawSecondOffset() - timeZone.getRawMillisecondOffset()/1000f;

            setTime(hour, minute, seconds);

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

    private class UpdateCompassRotation implements Runnable {

        private boolean end = false;

        @Override
        public void run() {

            if (viewModel.getRotateToNorth()) {

                float targetRotation = viewModel.getTargetRotation();
                float northRotation = binding.compassView.getNorthRotation();

                float diff = targetRotation - northRotation;
                northRotation += (targetRotation - northRotation)*0.05f * (Math.abs(diff) > Math.PI ? -1f : 1f);
                northRotation = (float) (northRotation > Math.PI ? northRotation - 2f*Math.PI: northRotation);
                northRotation = (float) (northRotation < -Math.PI ? northRotation + 2f*Math.PI: northRotation);

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

                boolean rotateToNorth = !viewModel.isRotateToNorth();

                setRotateToNorth(rotateToNorth);

                if (rotateToNorth) {
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