package com.skycompass;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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

    private MapViewModel viewModel;
    private FragmentMapBinding binding;
    private final LocationRequester locationRequester;

    public MapFragment() {

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
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

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
        binding.myLocation.setOnClickListener(new OnClickSetToMyLocation());

        updateOverlayLocation();
        updateMapViewLocation();

        notifyLocationChanged();

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

        binding.markerLocation.setText(Format.LatitudeLongitude(viewModel.getLatitude(), viewModel.getLongitude()));

        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), viewModel.useMyLocation() ? R.drawable.my_location : R.drawable.set_as_my_location, requireActivity().getTheme()
            )
        );

    }

    private void updateMapViewLocation() {

        binding.mapView.setMarkerLocation(viewModel.getLatitude(), viewModel.getLongitude());

    }

    private void notifyLocationChanged() {

        double latitude = viewModel.getLatitude();
        double longitude = viewModel.getLongitude();
        String description = viewModel.useMyLocation() && viewModel.hasMyLocation() ? getResources().getString(R.string.using_system_location) : null;

        Bundle bundle = new Bundle();

        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        bundle.putString("Location", description);

        getParentFragmentManager().setFragmentResult("MapFragment/LocationChanged", bundle);

    }

    public class OnMarkerDragListener implements Marker.OnMarkerDragListener {

        @Override
        public void onMarkerDrag(Marker marker) {

            double latitude = marker.getPosition().getLatitude();
            double longitude = marker.getPosition().getLongitude();

            Debug.log(String.format("Marker: %.2f %.2f", latitude, longitude));

            viewModel.setLocation(latitude, longitude, false);
            viewModel.setUseMyLocation(false);

            updateOverlayLocation();

            notifyLocationChanged();

        }

        @Override
        public void onMarkerDragEnd(Marker marker) { }

        @Override
        public void onMarkerDragStart(Marker marker) { }

    }

    private class OnClickSetToMyLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            LocationRequester.RequestState state = locationRequester.getPermissionState();

            switch (state) {
                case REQUESTED:
                    break;
                case GRANTED:

                    viewModel.setUseMyLocation(true);

                    if (viewModel.hasMyLocation()) {

                        Debug.log(String.format("System: %.2f %.2f", viewModel.getLatitude(), viewModel.getLongitude()));

                        updateOverlayLocation();
                        updateMapViewLocation();

                        notifyLocationChanged();

                    }

                    break;
                default:
                    locationRequester.request(MapFragment.this);
                    break;
            }

            if (locationRequester.getEnabledState() == LocationRequester.EnabledState.DISABLED)
                notifyUserLocationDisabled();

        }
    }

    private class OnRequestResult implements LocationRequester.OnRequestResult {
        @Override
        public void onResult(boolean granted) {

            if (granted) {

                viewModel.setUseMyLocation(true);

                // TODO Remove so updated with latest position - but why doesn't onLocationChanged trigger after request granted?
                if (viewModel.hasMyLocation()) {

                    Debug.log(String.format("System: %.2f %.2f", viewModel.getLatitude(), viewModel.getLongitude()));

                    updateOverlayLocation();
                    updateMapViewLocation();
                    notifyLocationChanged();

                }

            } else {

                binding.myLocation.setImageDrawable(
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

            viewModel.setLocation(latitude, longitude, true);

            if (viewModel.useMyLocation()) {

                Debug.log(String.format("System: %.2f %.2f", latitude, longitude));

                updateOverlayLocation();
                updateMapViewLocation();

                notifyLocationChanged();

            }

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