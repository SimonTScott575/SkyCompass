package com.skycompass;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
import com.skycompass.databinding.FragmentMainBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;
import com.skycompass.util.LocationRequester;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private OptionsFragment optionsFragment;
    private MapFragment mapFragment;
    private CalendarFragment calendarFragment;
    private ClockFragment clockFragment;

    private final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener();

    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();

    private final OnChangeOptionsListener onChangeOptionsListener = new OnChangeOptionsListener();

    private final OnClickUseSystemValue onClickUseSystemValue = new OnClickUseSystemValue();

    private Bundle locationBundle;
    private Bundle dateBundle;
    private Bundle timeBundle;

    private MainViewModel viewModel;

    private final LocationRequester locationRequester;

    public MainFragment() {

        locationRequester = new LocationRequester(new OnReceivedMyLocationRequester());

        locationRequester.setOnPermissionResult(new OnRequestResult());

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        locationRequester.register(this);

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = FragmentMainBinding.inflate(inflater, container, false);

        optionsFragment = new OptionsFragment();
        mapFragment = new MapFragment();
        calendarFragment = new CalendarFragment();
        clockFragment = new ClockFragment();

        switch (viewModel.currentOption) {
            case INFO:
                setInfo(optionsFragment);
                break;
            case MAP:
                setInfo(mapFragment);
                break;
            case CALENDAR:
                setInfo(calendarFragment);
                break;
            case CLOCK:
                setInfo(clockFragment);
                break;
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Bottom navigation
        switch (viewModel.currentFragment) {
            case INFO:
                setFragmentInfo();
                break;
            default:
                setFragmentCompass();
        }

        binding.bottomNavigation.setOnItemSelectedListener(onItemSelectedListener);

        // Options
        BottomSheetBehavior.from(binding.optionsBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

        binding.optionsNavigationCurrent.setOnClickListener(onClickUseSystemValue);
        binding.optionsNavigationBack.setOnClickListener(v -> {

            binding.optionsNavigationBack.setVisibility(View.INVISIBLE);
            binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

            getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.options_fragment_container, optionsFragment)
                    .commit();

            viewModel.currentOption = MainViewModel.OptionsFragment.INFO;

        });

        optionsFragment.getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), owner -> {
            if (owner != null) {

                viewModel.getLocationLiveData().observe(owner, location -> {
                    optionsFragment.setLocation(location, viewModel.isSystemLocation());
                });

                viewModel.getDateLiveData().observe(owner, date -> {
                    optionsFragment.setDate(date, viewModel.isSystemDate());
                });

                viewModel.getTimeLiveData().observe(owner, time -> {

                    int offset = viewModel.getZoneOffset().getTotalSeconds() * 1000;
                    String location = viewModel.getZoneId() != null ? viewModel.getZoneId().getId() : null;

                    optionsFragment.setTime(time, offset, location, viewModel.isSystemTime());

                });

            }
        });

        mapFragment.getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), owner -> {
            if (owner != null)
                viewModel.getLocationLiveData().observe(owner, location -> {
                    mapFragment.setLocation(location.latitude, location.longitude);
                });
        });

        calendarFragment.getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), owner -> {
            if (owner != null) {
                viewModel.getDateLiveData().observe(owner, date -> calendarFragment.setDate(date));
            }
        });

        clockFragment.getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), owner -> {
            if (owner != null) {
                viewModel.getDateLiveData().observe(owner, date -> {

                    if (viewModel.getZoneId() != null)
                        clockFragment.setZoneId(viewModel.getZoneId());
                    else
                        clockFragment.setZoneOffset(viewModel.getZoneOffset());

                    clockFragment.setDate(date);

                });
                viewModel.getTimeLiveData().observe(owner, time -> {

                    if (viewModel.getZoneId() != null)
                        clockFragment.setZoneId(viewModel.getZoneId());
                    else
                        clockFragment.setZoneOffset(viewModel.getZoneOffset());

                    clockFragment.setTime(time);

                });
            }
        });

        viewModel.getLocationLiveData().observe(getViewLifecycleOwner(), new LocationObserver());
        viewModel.getDateLiveData().observe(getViewLifecycleOwner(), new DateObserver());
        viewModel.getTimeLiveData().observe(getViewLifecycleOwner(), new TimeObserver());

    }

    @Override
    public void onResume() {
        super.onResume();

        getChildFragmentManager()
            .setFragmentResultListener("CalendarFragment/DateChanged", this, onChangeDateListener);
        getChildFragmentManager()
            .setFragmentResultListener("MapFragment/LocationChanged", this, onChangeLocationListener);
        getChildFragmentManager()
            .setFragmentResultListener("ClockFragment/TimeChanged", this, onChangeTimeListener);

        getChildFragmentManager()
            .setFragmentResultListener("OptionsFragment/ChangeOption", this, onChangeOptionsListener);

        viewModel.startRetrieveSystemValues();

        locationRequester.resume();

        // TODO
//        locationRequester.request(this);

    }

    @Override
    public void onPause() {
        super.onPause();

        locationRequester.pause();

        viewModel.endRetrieveSystemValues();

    }

    @Override
    public void onDetach() {

        locationRequester.unregister();

        super.onDetach();
    }

    private class OnItemSelectedListener implements NavigationBarView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int id = item.getItemId();

            Debug.log(String.format("Selected %d", id));

            Bundle bundle = new Bundle();
            if (locationBundle != null)
                bundle.putAll(locationBundle);
            if (dateBundle != null)
                bundle.putAll(dateBundle);
            if (timeBundle != null)
                bundle.putAll(timeBundle);

            if (id == R.id.bottom_item_compass) {

                if (viewModel.currentFragment == MainViewModel.FragmentView.COMPASS)
                    return false;

                getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_compass, CompassFragment.class, bundle)
                    .commit();

                setFragmentCompass();

                return true;

            }

            if (id == R.id.bottom_item_times) {

                if (viewModel.currentFragment == MainViewModel.FragmentView.INFO)
                    return false;

                getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_compass, InfoFragment.class, bundle)
                    .commit();

                setFragmentInfo();

                return true;

            }


            return false;
        }
    }

    private void setInfo(Fragment fragment) {

        getChildFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.options_fragment_container, fragment)
            .runOnCommit(() -> {

                if (fragment != optionsFragment)
                    binding.optionsNavigationBack.setVisibility(View.VISIBLE);

                if (fragment == mapFragment) {

                    binding.optionsNavigationCurrent.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    getResources(), viewModel.isSystemLocation() ? R.drawable.my_location : R.drawable.set_as_my_location, requireActivity().getTheme()
                            )
                    );

                    if (viewModel.isSystemLocation())
                        binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

                    viewModel.currentOption = MainViewModel.OptionsFragment.MAP;

                } else if (fragment == calendarFragment) {

                    binding.optionsNavigationCurrent.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.current_date, requireActivity().getTheme()
                            )
                    );

                    if (viewModel.isSystemDate())
                        binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

                    viewModel.currentOption = MainViewModel.OptionsFragment.CALENDAR;

                } else if (fragment == clockFragment) {

                    binding.optionsNavigationCurrent.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.current_time, requireActivity().getTheme()
                            )
                    );

                    if (viewModel.isSystemTime())
                        binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

                    viewModel.currentOption = MainViewModel.OptionsFragment.CLOCK;

                } else if (fragment == optionsFragment) {

                    binding.optionsNavigationBack.setVisibility(View.INVISIBLE);
                    binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

                }

            })
            .commit();

    }

    private class OnChangeOptionsListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            String message = result.getString("CHANGE", "");

            if (message.equals("LOCATION"))
                setInfo(mapFragment);
            else if (message.equals("DATE"))
                setInfo(calendarFragment);
            else if (message.equals("TIME"))
                setInfo(clockFragment);

        }
    }

    private class OnClickUseSystemValue implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.options_fragment_container);

            if (currentFragment == mapFragment) {

                LocationRequester.RequestState state = locationRequester.getPermissionState();

                switch (state) {
                    case REQUESTED:
                        break;
                    case GRANTED:
                        viewModel.useSystemLocation();
                        break;
                    default:
                        locationRequester.request(MainFragment.this);
                        break;
                }

                if (locationRequester.getEnabledState() == LocationRequester.EnabledState.DISABLED)
                    notifyUserLocationDisabled();

            } else if (currentFragment == calendarFragment) {

                viewModel.useSystemDate();

                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

            } else if (currentFragment == clockFragment) {

                viewModel.useSystemTime();

                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

            }

        }
    }

    private void setLocation(double latitude, double longitude, @Nullable String location) {

        Debug.log(String.format("Location: %.2f %.2f %s", latitude, longitude, location));

        viewModel.setLocation(new MainViewModel.Location(latitude, longitude));

    }

    private class LocationObserver implements Observer<MainViewModel.Location> {
        @Override
        public void onChanged(MainViewModel.Location location) {

            double latitude = location.latitude;
            double longitude = location.longitude;

            // TODO LiveData observers !
            switch (viewModel.currentFragment) {
                case COMPASS:
                    CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                    compassFragment.setLocation(longitude, latitude);
                    break;
                case INFO:
                    InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                    infoFragment.setLocation(longitude, latitude);
                    break;
            }

            if (getCurrentInfoFragment() == mapFragment)
                binding.optionsNavigationCurrent.setVisibility(viewModel.isSystemLocation() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class OnChangeLocationListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            locationBundle = result;

            setLocation(
                result.getDouble("Latitude"),
                result.getDouble("Longitude"),
                result.getString("Location")
            );

        }
    }

    private class OnRequestResult implements LocationRequester.OnRequestResult {
        @Override
        public void onResult(boolean granted) {

            if (granted) {
                // TODO ?? Remove so updated with latest position - but why doesn't onLocationChanged trigger after request granted?
                viewModel.useSystemLocation();
            } else {
                if (getChildFragmentManager().findFragmentById(R.id.options_fragment_container) == mapFragment)
                    binding.optionsNavigationCurrent.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            getResources(), R.drawable.no_location, requireActivity().getTheme()
                        )
                    );
                notifyUserLocationPermissionDenied();
            }

            if (locationRequester.getEnabledState() == LocationRequester.EnabledState.DISABLED)
                notifyUserLocationDisabled();

        }
    }

    private class OnReceivedMyLocationRequester implements LocationRequester.OnLocationChanged {
        @Override
        public void onLocationChanged(double latitude, double longitude) {
            viewModel.updateSystemLocation(new MainViewModel.Location(latitude, longitude));
        }
    }

    private void notifyUserLocationPermissionDenied() {
        try {
            Toast toast = Toast.makeText(requireContext(), "Location access denied in permission settings", Toast.LENGTH_LONG);
            toast.show();
        } catch (IllegalStateException e) {
            Debug.log("No context associated with MapFragment.");
        }
    }

    private void notifyUserLocationDisabled() {
        try {
            Toast toast = Toast.makeText(requireContext(), "Location updates disabled in settings", Toast.LENGTH_LONG);
            toast.show();
        } catch (IllegalStateException e) {
            Debug.log("No context associated with MapFragment.");
        }
    }

    private void setDate(LocalDate date) {

        Debug.log(String.format("Date: %s", date.toString()));

        viewModel.setDate(date);

        if (viewModel.isSystemTime())
            viewModel.useSystemTime();
        else
            viewModel.setTime(viewModel.getTimeLiveData().getValue());

    }

    private class DateObserver implements Observer<LocalDate> {

        @Override
        public void onChanged(LocalDate date) {

            // TODO LiveData observers !
            switch (viewModel.currentFragment) {
                case COMPASS:
                    CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                    compassFragment.setDate(date);
                    break;
                case INFO:
                    InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                    infoFragment.setDate(date);
                    break;
            }

            if (getCurrentInfoFragment() == calendarFragment)
                binding.optionsNavigationCurrent.setVisibility(viewModel.isSystemDate() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class OnChangeDateListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            dateBundle = result;

            setDate(
                LocalDate.of(result.getInt("Y"), result.getInt("M") + 1, result.getInt("D") + 1)
            );

        }
    }

    private void setTime(LocalTime time, int offset, String location) {

        Debug.log(String.format("Time: %s Offset: %d Location: %s", time.toString(), offset, location));

        if (location != null)
            viewModel.setZoneId(ZoneId.of(location));
        else
            viewModel.setZoneOffset(ZoneOffset.ofTotalSeconds(offset/1000));

        viewModel.setTime(time);

    }

    private class TimeObserver implements Observer<LocalTime> {

        @Override
        public void onChanged(LocalTime time) {

            int offset = viewModel.getZoneOffset().getTotalSeconds() * 1000;

            ZoneOffset timeZone = ZoneOffset.ofTotalSeconds(offset/1000);
            LocalDate dummy = LocalDate.of(2000,1,1);

            // TODO LiveData observers !
            switch (viewModel.currentFragment) {
                case COMPASS:
                    CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                    compassFragment.setTime(
                        ZonedDateTime.of(dummy, time, timeZone)
                            .withZoneSameInstant(ZoneOffset.ofHours(0))
                            .toLocalTime()
                    );
                    break;
                case INFO:
                    InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                    infoFragment.setTime(time, timeZone);
                    break;
            }

            if (getCurrentInfoFragment() == clockFragment)
                binding.optionsNavigationCurrent.setVisibility(viewModel.isSystemTime() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            timeBundle = result;

            LocalTime time = LocalTime.of(result.getInt("HOUR"), result.getInt("MINUTE"), result.getInt("SECOND"));
            String location = result.getString("LOCATION");
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(result.getInt("OFFSET")/1000);

            setTime(time, zoneOffset.getTotalSeconds()*1000, location);

        }
    }

    private void setFragmentCompass() {

        Debug.log("Focus: Compass");

        viewModel.currentFragment = MainViewModel.FragmentView.COMPASS;

    }

    private void setFragmentInfo() {

        Debug.log("Focus: Info");

        viewModel.currentFragment = MainViewModel.FragmentView.INFO;

    }

    private <T> T getChildFragment(Class<T> fragmentClass) {

        int id;

        if (fragmentClass == CompassFragment.class)
            id = R.id.fragment_compass;
        else if (fragmentClass == InfoFragment.class)
            id = R.id.fragment_compass;
        else if (fragmentClass == ClockFragment.class)
            id = R.id.clock_fragment_container;
        else
            throw new RuntimeException("Unrecognised fragment class.");

        FragmentManager manager = getChildFragmentManager();

        Fragment child = manager.findFragmentById(id);

        if (child != null && child.getClass() == fragmentClass)
            return (T) child;
        else
            throw new RuntimeException(String.format("Child fragment not instance of %s", fragmentClass.getSimpleName()));

    }

    private Fragment getCurrentInfoFragment() {
        return getChildFragmentManager().findFragmentById(R.id.options_fragment_container);
    }

}