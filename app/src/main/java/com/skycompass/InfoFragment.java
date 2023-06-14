package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.databinding.FragmentInfoBinding;
import com.skycompass.util.Format;
import com.skycompass.util.TimeZone;

import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.Body;
import io.github.cosinekitty.astronomy.Direction;
import io.github.cosinekitty.astronomy.MoonQuarterInfo;
import io.github.cosinekitty.astronomy.Observer;
import io.github.cosinekitty.astronomy.Time;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    double longitude, latitude;
    int year, month, dayOfMonth;
    int hour, minute, second; // Time in UTC
    TimeZone timeZone = new TimeZone(0); // UTC offset for displaying time

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

            setTime(hour, minute, seconds, UTCOffset);

        }

    }

    public void setLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        update();
    }

    public void setDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        update();
    }

    public void setTime(int hour, int minute, float second, int UTCOffset) {
        this.hour = hour;
        this.minute = minute;
        this.second = (int)second;
        this.timeZone = new TimeZone(UTCOffset);
        update();
    }

    private void update() {

        Observer observer = new Observer(latitude,longitude,0);
        Time time = new Time(year+1, month+1, dayOfMonth, hour, minute, second);
        int limitDays = 1;

        Time sunrise = Astronomy.searchRiseSet(Body.Sun, observer, Direction.Rise, time, limitDays);
        Time sunset = Astronomy.searchRiseSet(Body.Sun, observer, Direction.Set, time, limitDays);

        if (sunrise != null) {
            binding.sunriseTime.setText(timeToString(sunrise));
        } else {
            binding.sunriseTime.setText("N/A");
        }
        if (sunset != null) {
            binding.sunsetTime.setText(timeToString(sunset));
        } else {
            binding.sunsetTime.setText("N/A");
        }

    }

    private String timeToString(Time time) {
        com.skycompass.util.Time local = timeZone.timeFromUTC(new com.skycompass.util.Time(
            time.toDateTime().getHour(), time.toDateTime().getMinute(), (int)time.toDateTime().getSecond()
        ));
        return Format.Time(local.getHour(), local.getMinute(), (int) local.getSecond());
    }

}