package com.skycompass;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skycompass.databinding.FragmentMapBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;
import com.skycompass.util.LocationRequester;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private SystemViewModel systemViewModel;
    private FragmentMapBinding binding;

    private final LocationRequester locationRequester = new LocationRequester(new OnReceivedMyLocationRequester());

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        locationRequester.setOnPermissionResult(new OnRequestResult());

        locationRequester.register(this);

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentMapBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireActivity();

        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        binding.mapView.setUp();
        binding.mapView.setOnMarkerDragListener(new OnMarkerDragListener());

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), location -> {

            updateOverlayLocation();
            updateMapViewLocation();

        });

        systemViewModel.useSystemLocationLiveData().observe(getViewLifecycleOwner(), use -> {

            if (!use)
                return;

            LocationRequester.RequestState state = locationRequester.getPermissionState();

            switch (state) {
                case UNREQUESTED:
                case DENIED:
                    locationRequester.request(MapFragment.this);
                    break;
            }

            if (locationRequester.getEnabledState() == LocationRequester.EnabledState.DISABLED)
                notifyUserLocationDisabled();

        });

        binding.copyright.setMovementMethod(LinkMovementMethod.getInstance());
        binding.copyright.setClickable(true);
        binding.copyright.setText(Html.fromHtml(
            "© <a href='https://www.openstreetmap.org/copyright/en'>OpenStreetMap</a> contributors",
            Html.FROM_HTML_MODE_COMPACT
        ));

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.mapView.onResume();

        locationRequester.resume();

    }

    @Override
    public void onPause() {

        locationRequester.pause();

        binding.mapView.onPause();

        super.onPause();
    }

    @Override
    public void onDetach() {

        locationRequester.unregister();

        super.onDetach();
    }

    private void updateOverlayLocation() {

        SystemViewModel.Location location = systemViewModel.getLocationLiveData().getValue();

        binding.markerLocation.setText(Format.LatitudeLongitude(location.latitude, location.longitude));

    }

    private void updateMapViewLocation() {

        SystemViewModel.Location location = systemViewModel.getLocationLiveData().getValue();

        binding.mapView.setMarkerLocation(location.latitude, location.longitude);

    }

    public class OnMarkerDragListener implements Marker.OnMarkerDragListener {

        @Override
        public void onMarkerDrag(Marker marker) {

            double latitude = marker.getPosition().getLatitude();
            double longitude = marker.getPosition().getLongitude();

            Debug.log(String.format("Marker: %.2f %.2f", latitude, longitude));

            SystemViewModel.Location location = new SystemViewModel.Location(latitude,longitude);

            systemViewModel.setLocation(location);

        }

        @Override
        public void onMarkerDragEnd(Marker marker) { }

        @Override
        public void onMarkerDragStart(Marker marker) { }

    }

    private class OnRequestResult implements LocationRequester.OnRequestResult {
        @Override
        public void onResult(boolean granted) {

            if (!granted)
                notifyUserLocationPermissionDenied();

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

}