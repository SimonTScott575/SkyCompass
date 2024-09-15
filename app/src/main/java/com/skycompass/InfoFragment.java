package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skycompass.databinding.FragmentInfoBinding;
import com.skycompass.util.Format;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.Body;
import io.github.cosinekitty.astronomy.Direction;
import io.github.cosinekitty.astronomy.MoonQuarterInfo;
import io.github.cosinekitty.astronomy.Observer;
import io.github.cosinekitty.astronomy.Time;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    private SystemViewModel systemViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), location -> update());
        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), date -> update());
        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> update());

    }

    private void update() {

        SystemViewModel.Location location = systemViewModel.getLocationLiveData().getValue();
        LocalDateTime dateTime = LocalDateTime.of(
            systemViewModel.getDateLiveData().getValue(),
            systemViewModel.getTimeLiveData().getValue()
        );
        ZoneId timeZone = systemViewModel.getZoneId();

        ZonedDateTime utcDateTime = ZonedDateTime.of(dateTime.toLocalDate(), LocalTime.of(0,0,0), timeZone)
            .withZoneSameInstant(ZoneOffset.ofHours(0));
        LocalTime utcTime = utcDateTime.toLocalTime();
        LocalDate utcDate = utcDateTime.toLocalDate();

        Observer observer = new Observer(location.latitude,location.longitude,0);
        Time time = new Time(
            utcDate.getYear(), utcDate.getMonthValue(), utcDate.getDayOfMonth(),
            utcTime.getHour(), utcTime.getMinute(), utcTime.getSecond()
        );
        int limitDays = 1;

        Time sunrise = Astronomy.searchRiseSet(Body.Sun, observer, Direction.Rise, time, limitDays);
        Time sunset = Astronomy.searchRiseSet(Body.Sun, observer, Direction.Set, time, limitDays);
        Time moonrise = Astronomy.searchRiseSet(Body.Moon, observer, Direction.Rise, time, limitDays);
        Time moonset = Astronomy.searchRiseSet(Body.Moon, observer, Direction.Set, time, limitDays);

        setRiseSeTimeText(binding.sunriseTime, sunrise);
        setRiseSeTimeText(binding.sunsetTime, sunset);
        setRiseSeTimeText(binding.moonrise, moonrise);
        setRiseSeTimeText(binding.moonset, moonset);

        MoonQuarterInfo moonQuarterInfo = Astronomy.searchMoonQuarter(time);
        int quarter = moonQuarterInfo.getQuarter() == 0 ? 3 : moonQuarterInfo.getQuarter()-1;
        binding.moonPhase.setText(quarterName(quarter));
        binding.moonPhaseImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), quarterImage(quarter)));

    }

    private void setRiseSeTimeText(TextView textView, @Nullable Time time) {

        if (time != null) {
            textView.setText(timeToString(time));
        } else {
            textView.setText("N/A");
        }

    }

    private String timeToString(Time time) {

        LocalTime UTCTime = LocalTime.of(
            time.toDateTime().getHour(),
            time.toDateTime().getMinute(),
            (int)time.toDateTime().getSecond()
        );
        LocalDate UTCDate = LocalDate.of(
            time.toDateTime().getYear(),
            time.toDateTime().getMonth(),
            time.toDateTime().getDay()
        );

        return Format.Time(
            ZonedDateTime.of(UTCDate, UTCTime, ZoneOffset.ofHours(0))
                .withZoneSameInstant(systemViewModel.getZoneOffset())
                .toLocalTime()
        );

    }

    private static String quarterName(int quarter) {
        switch (quarter) {
            case 0: return "New Moon / First Quarter";
            case 1: return "First Quarter / Full Moon";
            case 2: return "Full Moon / Third Quarter";
            case 3: return "Third Quarter / New Moon";
            default: return "INVALID QUARTER";
        }
    }

    private static int quarterImage(int quarter) {
        switch (quarter) {
            case 0: return R.drawable.moon_new_first;
            case 1: return R.drawable.moon_first_full;
            case 2: return R.drawable.moon_full_third;
            case 3: return R.drawable.moon_third_new;
            default: return -1;
        }
    }

}