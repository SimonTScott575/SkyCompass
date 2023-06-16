package com.skycompass;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private MapViewModel viewModel;
    private FragmentMapBinding binding;
    private final LocationRequester locationRequester;

    public MapFragment() {
        locationRequester = new LocationRequester(new OnReceivedMyLocationRequester());
        locationRequester.setOnPermissionResult(new OnPermissionResult());
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

        Context ctx = requireActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding.mapView.setUp();
        binding.mapView.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

                double longitude = marker.getPosition().getLongitude();
                double latitude = marker.getPosition().getLatitude();

                setLocationAsMapMarker(latitude, longitude, null);

            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });

        binding.myLocation.setOnClickListener(new OnClickSetToMyLocation());

        if (viewModel.autoSetAsMyLocation() && viewModel.getMyLocation() != null) {
            setLocationFromMyLocation();
        } else {
            setLocation(
                viewModel.getMarkerLongitude(), viewModel.getMarkerLatitude(),
                viewModel.getMarkerLocationDescription()
            );
        }

        binding.copyright.setMovementMethod(LinkMovementMethod.getInstance());
        binding.copyright.setClickable(true);
        binding.copyright.setText(Html.fromHtml("© <a href='https://www.openstreetmap.org/copyright/en'>OpenStreetMap</a> contributors", Html.FROM_HTML_MODE_COMPACT));

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

    public void setMyLocation(double longitude, double latitude) {
        viewModel.setMyLocation(new GeoPoint(latitude, longitude));
        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), R.drawable.set_as_my_location, requireActivity().getTheme()
            )
        );
    }

    public void setLocation(double longitude, double latitude, String location) {

        binding.mapView.setMarkerLocation(latitude, longitude);
        setLocationAsMapMarker(latitude, longitude, location);

    }

    private void setLocationAsMapMarker(double latitude, double longitude, String location) {

        viewModel.setAutoSetAsMyLocation(false);
        viewModel.setMarkerLocation(latitude, longitude, location);

        binding.markerLocation.setText(Format.LatitudeLongitude(latitude, longitude));
        if (viewModel.getMyLocation() != null) {
            binding.myLocation.setImageDrawable(
                ResourcesCompat.getDrawable(
                    getResources(), R.drawable.set_as_my_location, requireActivity().getTheme()
                )
            );
        }

        onLocationChanged(longitude, latitude, location);

    }

    public void setLocationFromMyLocation() {

        viewModel.setAutoSetAsMyLocation(true);

        if (viewModel.getMyLocation() == null) {
            return;
        }
        setLocationAsMyLocation(viewModel.getMyLocation().getLatitude(), viewModel.getMyLocation().getLongitude());

    }

    private void setLocationAsMyLocation(double latitude, double longitude) {

        String description = getResources().getString(R.string.using_system_location);

        viewModel.setMarkerLocation(latitude, longitude, description);

        binding.mapView.setMarkerLocation(latitude, longitude);
        binding.markerLocation.setText(Format.LatitudeLongitude(latitude, longitude));
        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), R.drawable.my_location, requireActivity().getTheme()
            )
        );

        onLocationChanged(longitude, latitude, description);

    }

    public void onLocationChanged(double longitude, double latitude, String location) {

        Bundle bundle = new Bundle();
        bundle.putDouble("Longitude", longitude);
        bundle.putDouble("Latitude", latitude);
        bundle.putString("Location", location);
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("B", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    private class OnClickSetToMyLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            LocationRequester.PermissionState state = locationRequester.permissionState();

            switch (state) {
                case REQUESTED:
                    break;
                case GRANTED:
                    setLocationFromMyLocation();
                    break;
                default:
                    locationRequester.request(MapFragment.this);
                    break;
            }

            if (locationRequester.enabled() == LocationRequester.EnabledState.DISABLED) {
                notifyUserLocationDisabled();
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

    private class OnReceivedMyLocationRequester implements LocationRequester.OnLocationChanged {
        @Override
        public void onLocationChanged(double latitude, double longitude) {

            boolean differentLatitude = true;
            boolean differentLongitude = true;
            if (viewModel.getMyLocation() != null) {
                differentLatitude = viewModel.getMyLocation().getLatitude() != latitude;
                differentLongitude = viewModel.getMyLocation().getLongitude() != longitude;
            }

            if (differentLatitude || differentLongitude) {
                setMyLocation(longitude, latitude);
                setLocationAsMyLocation(latitude, longitude);
            }

        }
    }

    private class OnPermissionResult implements LocationRequester.OnPermissionResult {
        @Override
        public void onResult(boolean granted) {

            if (granted) {
                setLocationFromMyLocation();
            } else {

                binding.myLocation.setImageDrawable(
                        ResourcesCompat.getDrawable(
                                getResources(), R.drawable.no_location, requireActivity().getTheme()
                        )
                );

                notifyUserLocationPermissionDenied();

            }

            if (locationRequester.enabled() == LocationRequester.EnabledState.DISABLED) {
                notifyUserLocationDisabled();
            }

        }
    }

    private FragmentManager getParentFragmentManagerOrThrowException()
    throws NoParentFragmentManagerAttachedException {

        try {
            return getParentFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoParentFragmentManagerAttachedException();
        }

    }

    private static class NoParentFragmentManagerAttachedException extends Debug.Exception {
        public NoParentFragmentManagerAttachedException() {
            super("No parent fragment manager attached.");
        }
    }

}