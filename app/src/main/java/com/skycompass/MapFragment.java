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

import com.skycompass.databinding.FragmentMapBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private SystemViewModel systemViewModel;
    private FragmentMapBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

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

    }

    @Override
    public void onPause() {

        binding.mapView.onPause();

        super.onPause();
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

}