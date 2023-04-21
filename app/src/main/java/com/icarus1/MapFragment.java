package com.icarus1;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.databinding.FragmentMapBinding;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private MapViewModel viewModel;
    private FragmentMapBinding binding;
    private final LocationRequester locationRequester;

    public MapFragment() {
        locationRequester = new LocationRequester((latitude, longitude) -> {

            setMyLocation(longitude, latitude);

            if (viewModel.getMyLocation() != null && viewModel.autoSetAsMyLocation()) {
                boolean differentLatitude = viewModel.getMyLocation().getLatitude() != viewModel.getMarkerLatitude();
                boolean differentLongitude = viewModel.getMyLocation().getLongitude() != viewModel.getMarkerLongitude();
                if (differentLatitude || differentLongitude) {
                    setLocationAsMyLocation();
                }
            }

        });
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

        setUpMap();

    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
        locationRequester.request(this);
    }

    @Override
    public void onPause() {
        locationRequester.cancel();
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDetach() {
        locationRequester.unregister();
        super.onDetach();
    }

    private void setUpMap() {

        binding.mapView.setUp();

        binding.mapView.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

                double longitude = marker.getPosition().getLongitude();
                double latitude = marker.getPosition().getLatitude();

                setLocationFromMapMarker(latitude, longitude, null);

            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });

        binding.myLocation.setOnClickListener(new OnClickSetToMyLocation());

        boolean autoSetAsMyLocation = viewModel.autoSetAsMyLocation();
        setLocation(
            viewModel.getMarkerLongitude(), viewModel.getMarkerLatitude(),
            viewModel.getMarkerLocationDescription()
        );
        setAutoSetAsMyLocation(autoSetAsMyLocation);

    }

    public void setLocation(double longitude, double latitude, String location) {

        binding.mapView.setMarkerLocation(latitude, longitude);
        setLocationFromMapMarker(latitude, longitude, location);

    }

    private void setLocationFromMapMarker(double latitude, double longitude, String location) {

        setAutoSetAsMyLocation(false);

        binding.markerLocation.setText(Format.LatitudeLongitude(latitude, longitude));
        if (viewModel.getMyLocation() != null) {
            binding.myLocation.setImageDrawable(
                ResourcesCompat.getDrawable(
                    getResources(), R.drawable.set_as_my_location, requireActivity().getTheme()
                )
            );
        }

        viewModel.setMarkerLocation(latitude, longitude, location);

        onLocationChanged(longitude, latitude, location);

    }

    public void setLocationAsMyLocation() {

        if (viewModel.getMyLocation() == null) {
            return;
        }

        double latitude = viewModel.getMyLocation().getLatitude();
        double longitude = viewModel.getMyLocation().getLongitude();
        String description = getResources().getString(R.string.using_system_location);

        binding.mapView.setMarkerLocation(latitude, longitude);
        binding.markerLocation.setText(Format.LatitudeLongitude(latitude, longitude));
        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), R.drawable.my_location, requireActivity().getTheme()
            )
        );

        viewModel.setMarkerLocation(latitude, longitude, description);

        onLocationChanged(longitude, latitude, description);

    }

    public void setAutoSetAsMyLocation(boolean auto) {
        viewModel.setAutoSetAsMyLocation(auto);
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

    public void setMyLocation(double longitude, double latitude) {
        viewModel.setMyLocation(new GeoPoint(latitude, longitude));
        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), R.drawable.set_as_my_location, requireActivity().getTheme()
            )
        );
    }

    public void unsetMyLocation() {
        viewModel.setMyLocation(null);
        binding.myLocation.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(), R.drawable.no_location, requireActivity().getTheme()
            )
        );
    }

    private class OnClickSetToMyLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setAutoSetAsMyLocation(true);
            setLocationAsMyLocation();
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