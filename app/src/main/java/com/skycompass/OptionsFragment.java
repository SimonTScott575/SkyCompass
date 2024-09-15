package com.skycompass;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.databinding.FragmentOptionsBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OptionsFragment extends Fragment {

    private OptionsViewModel viewModel;

    private FragmentOptionsBinding binding;

    private OnClickChangeOption onClickChangeOption = new OnClickChangeOption();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(OptionsViewModel.class);

        binding = FragmentOptionsBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.changeLocation.setOnClickListener(onClickChangeOption);
        binding.changeDate.setOnClickListener(onClickChangeOption);
        binding.changeTime.setOnClickListener(onClickChangeOption);

        updateLocation();
        updateDate();
        updateTime();

    }

    private class OnClickChangeOption implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            Bundle bundle = new Bundle();

            String message = null;

            if (view == binding.changeLocation)
                message = "LOCATION";
            else if (view == binding.changeDate)
                message = "DATE";
            else if (view == binding.changeTime)
                message = "TIME";

            if (message != null) {

                bundle.putString("CHANGE", message);

                getParentFragmentManager().setFragmentResult("OptionsFragment/ChangeOption", bundle);

            }

        }
    }

    public void setDate(LocalDate date, boolean isSystemDate) {

        Debug.log(String.format("Date: %s System date: %b", date.toString(), isSystemDate));

        viewModel.setDate(date);
        viewModel.setSystemDate(isSystemDate);

        updateDate();

    }

    private void updateDate() {

        LocalDate date = viewModel.getDate();

        boolean isCurrentDate = viewModel.isSystemDate();

        binding.dateText.setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        if (isCurrentDate)
            binding.dateSubscript.setText(R.string.using_system_date);
        else
            binding.dateSubscript.setText(R.string.tap_to_change_date);

    }

    public void setTime(LocalTime time, int offset, String description, boolean isSystemTime) {

        Debug.log(String.format("Time: %s Time zone offset: %d Description: %s System time: %b", time.toString(), offset, description, isSystemTime));

        viewModel.setTime(time);
        viewModel.setTimeZoneOffset(offset);
        viewModel.setTimeDescription(description);
        viewModel.setSystemTime(isSystemTime);

        updateTime();

    }

    private void updateTime() {

        LocalTime time = viewModel.getTime();
        int offset = viewModel.getTimeZoneOffset();
        String description = viewModel.getTimeDescription();

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (UTC" + Format.TimeZoneOffset(offset) + ")";

        binding.timeText.setText(text);

        if (description != null)
            binding.timeLocation.setText(description);
        else
            binding.timeLocation.setText(R.string.tap_to_change_time);

    }

    public void setLocation(MainViewModel.Location location, boolean isSystemLocation) {

        Debug.log(String.format("Location: %.2f %.2f System location: %b", location.latitude, location.longitude, isSystemLocation));

        viewModel.setLocation(location);
        viewModel.setSystemLocation(isSystemLocation);

        updateLocation();

    }

    private void updateLocation() {

        MainViewModel.Location location = viewModel.getLocation();

        binding.locationText.setText(Format.LatitudeLongitude(location.latitude, location.longitude));

        if (viewModel.isSystemLocation())
            binding.locationAddress.setText(R.string.current_location);
        else
            binding.locationAddress.setText(R.string.tap_to_change_location);

    }

}