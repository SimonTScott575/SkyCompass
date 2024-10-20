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

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private MainViewModel viewModel;
    private SystemViewModel systemViewModel;

    private final OnBackStackChangedListener onBackStackChangedListener = new OnBackStackChangedListener();
    private final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener();
    private final OnClickUseSystemValue onClickUseSystemValue = new OnClickUseSystemValue();

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
        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentMainBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        boolean isPortrait = requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        boolean isLandscape = requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        // Bottom navigation
        switch (viewModel.currentFragment) {
            case INFO:
                setFragmentInfo();
                break;
            default:
                setFragmentCompass();
        }

        if (isPortrait)
            binding.bottomNavigation.setOnItemSelectedListener(onItemSelectedListener);
        else if (isLandscape)
            binding.viewPager.setAdapter(new ViewPagerAdapter(this));

        // Options
        if (isPortrait)
            BottomSheetBehavior.from(binding.optionsBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        else if (isLandscape)
            SideSheetBehavior.from(binding.optionsSideSheet).setState(SideSheetBehavior.STATE_EXPANDED);

        binding.optionsNavigationCurrent.setOnClickListener(onClickUseSystemValue);
        binding.optionsNavigationBack.setOnClickListener(v -> getChildFragmentManager().popBackStack());

        if (getChildFragmentManager().findFragmentByTag("OPTION") == null)
            requestOptionFragment();

        SystemObserver observer = new SystemObserver();

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), observer);
        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), observer);
        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), observer);
        systemViewModel.getZoneOffsetLiveData().observe(getViewLifecycleOwner(), observer);

        //
        if (isPortrait) {
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

        getChildFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        systemViewModel.startRetrieveSystemValues();

        locationRequester.resume();

    }

    @Override
    public void onPause() {
        super.onPause();

        locationRequester.pause();

        systemViewModel.endRetrieveSystemValues();

        getChildFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);

    }

    @Override
    public void onDetach() {

        locationRequester.unregister();

        super.onDetach();
    }

    private class OnBackStackChangedListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            updateOptionView();
        }
    }

    private void requestOptionFragment() {

        getChildFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.options_fragment_container, OptionsFragment.class, null, "OPTION")
            .commit();

    }

    private void updateOptionView() {

        int drawable = 0;

        String tag = getOptionFragment().getTag();

        switch (tag) {
            case "LOCATION":
                drawable = systemViewModel.isSystemLocation() ? R.drawable.my_location : R.drawable.set_as_my_location;
                break;
            case "DATE":
                drawable = systemViewModel.isSystemDate() ? 0 : R.drawable.current_date;
                break;
            case "TIME_ZONE":
            case "TIME":
                drawable = systemViewModel.isSystemTime() ? 0 : R.drawable.current_time;
                break;
        }

        if (drawable == 0) {

            binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);

        } else {

            binding.optionsNavigationCurrent.setImageDrawable(
                ResourcesCompat.getDrawable(
                    getResources(), drawable, requireActivity().getTheme()
                )
            );

            binding.optionsNavigationCurrent.setVisibility(View.VISIBLE);

        }

        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            binding.optionsNavigationBack.setVisibility(View.INVISIBLE);
            binding.optionsNavigationCurrent.setVisibility(View.INVISIBLE);
        } else {
            binding.optionsNavigationBack.setVisibility(View.VISIBLE);
        }

    }

    private Fragment getOptionFragment() {
        return getChildFragmentManager().findFragmentById(R.id.options_fragment_container);
    }

    private class SystemObserver implements Observer<Object> {
        @Override
        public void onChanged(Object o) {
            updateOptionView();
        }
    }

    private class OnClickUseSystemValue implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            String tag = getOptionFragment().getTag();

            switch (tag) {
                case "LOCATION":

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

                    break;
                case "DATE":

                    systemViewModel.useSystemDate();

                    break;
                case "TIME_ZONE":
                case "TIME":

                    systemViewModel.useSystemTime();

                    break;
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

                String tag = getChildFragmentManager().findFragmentById(R.id.options_fragment_container).getTag();

                if (tag != null && tag.equals("LOCATION"))
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

        viewModel.currentFragment = MainViewModel.FragmentView.COMPASS;

    }

    private void setFragmentInfo() {

        Debug.log("Focus: Info");

        viewModel.currentFragment = MainViewModel.FragmentView.INFO;

    }

    private class OnItemSelectedListener implements NavigationBarView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int id = item.getItemId();

            Debug.log(String.format("Selected %d", id));

            // TODO bundle data ?

            if (id == R.id.bottom_item_compass) {

                if (viewModel.currentFragment == MainViewModel.FragmentView.COMPASS)
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

                if (viewModel.currentFragment == MainViewModel.FragmentView.INFO)
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