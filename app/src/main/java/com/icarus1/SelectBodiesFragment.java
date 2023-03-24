package com.icarus1;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.compass.CelestialBody;
import com.icarus1.databinding.FragmentSelectBodiesBinding;
import com.icarus1.databinding.SelectableBodyBinding;
import com.icarus1.views.ScrollTable;

public class SelectBodiesFragment extends Fragment {

    private SelectBodiesViewModel mViewModel;
    private FragmentSelectBodiesBinding binding;

    public static SelectBodiesFragment newInstance() {
        return new SelectBodiesFragment();
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        binding = FragmentSelectBodiesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        for (CelestialBody body : CelestialBody.values()) {

            SelectableBodyBinding selectableBodyBinding = SelectableBodyBinding.inflate(getLayoutInflater(), binding.getRoot().findViewById(R.id.bodies_table), true);
            selectableBodyBinding.name.setText(body.getName());

        }

        ScrollTable scrollTable = binding.getRoot().findViewById(R.id.custom_linear_layout);
        scrollTable.setHeader(R.layout.selectable_body_header);

/*
        ScrollTable scrollTable = binding.getRoot().findViewById(R.id.custom_linear_layout);
        ScrollView customScrollView = scrollTable.findViewById(R.id.custom_scroll_view);
        TableLayout customTableLayout = customScrollView.findViewById(R.id.bodies_table);
        TableRow customTableHeader = customTableLayout.findViewById(R.id.custom_header);

        if (customTableHeader == null) {
            return;
        }

//        customTableLayout.setBackgroundColor(Color.argb(1f,1f,1f,1f));

        customTableLayout.removeView(customTableHeader);
        customTableHeader.setBackgroundColor(Color.argb(1f,1f,1f,1f));
        scrollTable.setHeader(customTableHeader);

        View v = getLayoutInflater().inflate(R.layout.selectable_body_header, customTableLayout, false);
        v.setScaleY(0);
        customTableLayout.addView(v,0);
*/

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SelectBodiesViewModel.class);
        // TODO: Use the ViewModel
    }

}