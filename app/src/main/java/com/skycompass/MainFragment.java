package com.skycompass;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.skycompass.databinding.FragmentMainBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.LocationRequester;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private OptionsFragment optionsFragment;
    private MapFragment mapFragment;
    private CalendarFragment calendarFragment;
    private ClockFragment clockFragment;

    private final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener();

    private final OnChangeOptionsListener onChangeOptionsListener = new OnChangeOptionsListener();

    private final OnClickUseSystemValue onClickUseSystemValue = new OnClickUseSystemValue();

    private MainViewModel2 viewModel;
    private SystemViewModel systemViewModel;

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

        viewModel = new ViewModelProvider(this).get(MainViewModel2.class);
        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

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

        if (requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            binding.bottomNavigation.setOnItemSelectedListener(onItemSelectedListener);
        else if (requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            binding.viewPager.setAdapter(new ViewPagerAdapter(this));

        // Options
        if (requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            BottomSheetBehavior.from(binding.optionsBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        else if (requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SideSheetBehavior.from(binding.optionsSideSheet).setState(SideSheetBehavior.STATE_EXPANDED);
        }

        binding.optionsNavigationCurrent.setOnClickListener(onClickUseSystemValue);
        binding.optionsNavigationBack.setOnClickListener(v -> {

            if (getChildFragmentManager().getBackStackEntryCount() == 0)
                return;

            if (getChildFragmentManager().getBackStackEntryCount() == 1) {
                binding.optionsNavigationBack.setVisibility(View.INVISIBLE);
                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);
                viewModel.currentOption = MainViewModel2.OptionsFragment.INFO;
            }

            getChildFragmentManager().popBackStack();

        });

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), new LocationObserver());
        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new DateObserver());
        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), new TimeObserver());
        systemViewModel.getZoneOffsetLiveData().observe(getViewLifecycleOwner(), new ZoneOffsetObserver());

        //
        if (requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.optionsBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(() ->
                binding.fragmentCompass.setLayoutParams(new ViewGroup.LayoutParams(
                    binding.fragmentCompass.getMeasuredWidth(), (int)
                    binding.optionsCoordinatorLayout.getY() + (int) binding.optionsBottomSheet.getY())
                )
            );
        }

    }



    @Override
    public void onResume() {
        super.onResume();

        getChildFragmentManager()
            .setFragmentResultListener("OptionsFragment/ChangeOption", this, onChangeOptionsListener);

        getChildFragmentManager().addOnBackStackChangedListener(() -> {

            if (getChildFragmentManager().getBackStackEntryCount() == 0) {

                binding.optionsNavigationBack.setVisibility(View.INVISIBLE);
                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

                viewModel.currentOption = MainViewModel2.OptionsFragment.INFO;

            }

        });

        systemViewModel.startRetrieveSystemValues();

        locationRequester.resume();

    }

    @Override
    public void onPause() {
        super.onPause();

        locationRequester.pause();

        systemViewModel.endRetrieveSystemValues();

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

            // TODO bundle data ?

            if (id == R.id.bottom_item_compass) {

                if (viewModel.currentFragment == MainViewModel2.FragmentView.COMPASS)
                    return false;

                getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_compass, CompassFragment.class, null)
                    .commit();

                setFragmentCompass();

                return true;

            }

            if (id == R.id.bottom_item_times) {

                if (viewModel.currentFragment == MainViewModel2.FragmentView.INFO)
                    return false;

                getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_compass, InfoFragment.class, null)
                    .commit();

                setFragmentInfo();

                return true;

            }


            return false;
        }
    }

    private void setInfo(Fragment fragment) {

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.options_fragment_container, fragment);

        if (fragment != optionsFragment)
            transaction.addToBackStack(null);

        transaction.commit();

        int drawable = 0;

        if (fragment == mapFragment) {

            drawable = systemViewModel.isSystemLocation() ? R.drawable.my_location : R.drawable.set_as_my_location;

            binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemLocation() ? View.INVISIBLE : View.VISIBLE);

            viewModel.currentOption = MainViewModel2.OptionsFragment.MAP;

        } else if (fragment == calendarFragment) {

            drawable = R.drawable.current_date;

            binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemDate() ? View.INVISIBLE : View.VISIBLE);

            viewModel.currentOption = MainViewModel2.OptionsFragment.CALENDAR;

        } else if (fragment == clockFragment) {

            drawable = R.drawable.current_time;

            binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemTime() ? View.INVISIBLE : View.VISIBLE);

            viewModel.currentOption = MainViewModel2.OptionsFragment.CLOCK;

        }

        if (fragment != optionsFragment) {

            binding.optionsNavigationBack.setVisibility(View.VISIBLE);

            binding.optionsNavigationCurrent.setImageDrawable(
                ResourcesCompat.getDrawable(
                    getResources(), drawable, requireActivity().getTheme()
                )
            );

        }

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

    private class LocationObserver implements Observer<SystemViewModel.Location> {
        @Override
        public void onChanged(SystemViewModel.Location location) {

            if (getCurrentInfoFragment() == mapFragment)
                binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemLocation() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class DateObserver implements Observer<LocalDate> {
        @Override
        public void onChanged(LocalDate date) {

            if (getCurrentInfoFragment() == calendarFragment)
                binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemDate() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class TimeObserver implements Observer<LocalTime> {
        @Override
        public void onChanged(LocalTime time) {

            if (getCurrentInfoFragment() == clockFragment)
                binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemTime() ? View.INVISIBLE : View.VISIBLE);

        }
    }

    private class ZoneOffsetObserver implements Observer<ZoneOffset> {
        @Override
        public void onChanged(ZoneOffset zoneOffset) {

            if (getCurrentInfoFragment() == clockFragment || getCurrentInfoFragment() instanceof TimeZoneFragment)
                binding.optionsNavigationCurrent.setVisibility(systemViewModel.isSystemTime() ? View.INVISIBLE : View.VISIBLE);

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
                        systemViewModel.useSystemLocation();
                        break;
                    default:
                        locationRequester.request(MainFragment.this);
                        break;
                }

                if (locationRequester.getEnabledState() == LocationRequester.EnabledState.DISABLED)
                    notifyUserLocationDisabled();

            } else if (currentFragment == calendarFragment) {

                systemViewModel.useSystemDate();

                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

            } else if (currentFragment == clockFragment || currentFragment instanceof TimeZoneFragment) {

                systemViewModel.useSystemTime();

                binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

            }

        }
    }

    private class OnRequestResult implements LocationRequester.OnRequestResult {
        @Override
        public void onResult(boolean granted) {

            if (granted) {
                // TODO ?? Remove so updated with latest position - but why doesn't onLocationChanged trigger after request granted?
                systemViewModel.useSystemLocation();
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
            systemViewModel.updateSystemLocation(new SystemViewModel.Location(latitude, longitude));
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

    private void setFragmentCompass() {

        Debug.log("Focus: Compass");

        viewModel.currentFragment = MainViewModel2.FragmentView.COMPASS;

    }

    private void setFragmentInfo() {

        Debug.log("Focus: Info");

        viewModel.currentFragment = MainViewModel2.FragmentView.INFO;

    }

    private Fragment getCurrentInfoFragment() {
        return getChildFragmentManager().findFragmentById(R.id.options_fragment_container);
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(Fragment fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new CompassFragment();
                case 1:
                    return new InfoFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


}