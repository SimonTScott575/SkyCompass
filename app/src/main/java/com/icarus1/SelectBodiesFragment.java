package com.icarus1;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TableRow;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.icarus1.compass.CelestialBody;
import com.icarus1.databinding.FragmentSelectBodiesBinding;
import com.icarus1.databinding.ViewSelectableBodyBinding;

public class SelectBodiesFragment extends Fragment {

    private SelectBodiesViewModel viewModel;
    private FragmentSelectBodiesBinding binding;

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

            ViewSelectableBodyBinding selectableBodyBinding = ViewSelectableBodyBinding.inflate(getLayoutInflater(), binding.bodiesTable, true);
            selectableBodyBinding.name.setText(body.getName());

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        for (CelestialBody body : CelestialBody.values()) {

            setView(body, viewModel.getCheck(body));

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        for (CelestialBody body : CelestialBody.values()) {

            CheckBox checkBox = binding.bodiesTable.getChildAt(body.getIndex()+1).findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new OnCheckListener(body.getIndex()));

        }


    }

    @Override
    public void onPause() {

        for (CelestialBody body : CelestialBody.values()) {

            CheckBox checkBox = binding.bodiesTable.getChildAt(body.getIndex()+1).findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(null);

        }

        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SelectBodiesViewModel.class);
    }

    public void setView(CelestialBody body, boolean view) {

        TableRow row = (TableRow) binding.bodiesTable.getChildAt(body.getIndex()+1);
        CheckBox checkBox = row.findViewById(R.id.checkBox);
        checkBox.setChecked(view);

        viewModel.setView(body, view);

        onCheckView(body, view, body.getIndex());

    }

    private class OnCheckListener implements CompoundButton.OnCheckedChangeListener {

        private int index;

        public OnCheckListener(int index) {
            this.index = index;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            setView(CelestialBody.values()[index], isChecked);

        }

    }

    public void onCheckView(CelestialBody body, boolean isChecked, int index) {

        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
        bundle.putBoolean("CHECKED", isChecked);

        requireActivity().getSupportFragmentManager().setFragmentResult("E_"+body.getName(), bundle);

    }

}