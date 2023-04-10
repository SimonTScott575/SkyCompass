package com.icarus1.map;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.LocationRequester;
import com.icarus1.MainFragment;
import com.icarus1.R;
import com.icarus1.databinding.FragmentMapBinding;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {
    //TODO create userLocation in MapViewModel

    private MapViewModel viewModel;
    private FragmentMapBinding binding;
    private final LocationRequester locationRequester;

    public MapFragment() {
        locationRequester = new LocationRequester((latitude, longitude) -> setUserLocation(longitude, latitude));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        locationRequester.register(requireActivity());

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
        locationRequester.request();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        binding.mapView.onPause();
        locationRequester.cancel();
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

                setLocation(longitude, latitude, null);

                if (viewModel.getMyLocation() != null) {
                    binding.myLocation.setVisibility(View.VISIBLE);
                    binding.myLocationText.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });

        int myLocationVisibility = viewModel.getMyLocation() != null ? View.VISIBLE : View.INVISIBLE;
        binding.myLocation.setOnClickListener(new OnClickSetToLocation());
        binding.myLocation.setVisibility(myLocationVisibility);
        binding.myLocationText.setVisibility(myLocationVisibility);

        setLocation(viewModel.getMarkerLongitude(), viewModel.getMarkerLatitude(), viewModel.getMarkerLocationDescription());

    }

    public void setLocation(double longitude, double latitude, String location) {

        binding.mapView.setMarkerLocation(latitude, longitude);
        binding.markerLocation.setText(Format.LatitudeLongitude(latitude, longitude));

        viewModel.setMarkerLocation(latitude, longitude, location);

        onLocationChanged(longitude, latitude, location);

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

    public void setUserLocation(double longitude, double latitude) {

        viewModel.setMyLocation(new GeoPoint(latitude, longitude));
        binding.myLocation.setVisibility(View.VISIBLE);
        binding.myLocationText.setVisibility(View.VISIBLE);

    }

    private class OnClickSetToLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            GeoPoint myLocation = viewModel.getMyLocation();

            binding.mapView.setMarkerLocation(myLocation.getLatitude(), myLocation.getLongitude());

            binding.myLocation.setVisibility(View.INVISIBLE);
            binding.myLocationText.setVisibility(View.INVISIBLE);

            double longitude = viewModel.getMyLocation().getLongitude();
            double latitude = viewModel.getMyLocation().getLatitude();

            setLocation(longitude, latitude, getResources().getString(R.string.using_system_location));

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