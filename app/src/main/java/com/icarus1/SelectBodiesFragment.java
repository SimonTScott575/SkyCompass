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

            SelectableBodyBinding selectableBodyBinding = SelectableBodyBinding.inflate(getLayoutInflater(), binding.bodiesLinearLayout, true);
            selectableBodyBinding.textView5.setText(body.getName());

        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SelectBodiesViewModel.class);
        // TODO: Use the ViewModel
    }

}