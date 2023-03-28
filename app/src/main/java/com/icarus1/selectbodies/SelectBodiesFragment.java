package com.icarus1.selectbodies;

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
import android.widget.TableRow;

import com.icarus1.R;
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
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentSelectBodiesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SelectBodiesViewModel.class);

        for (CelestialBody body : CelestialBody.values()) {

            ViewSelectableBodyBinding selectableBodyBinding = ViewSelectableBodyBinding.inflate(getLayoutInflater(), binding.bodiesTable, true);
            selectableBodyBinding.name.setText(body.getName());
            selectableBodyBinding.checkBox.setOnCheckedChangeListener(new OnCheckListener(body.getIndex()));

            setViewable(body, viewModel.getViewable(body));

        }

    }

    public boolean getViewable(CelestialBody body) {
        return viewModel.getViewable(body);
    }

    public void setViewable(CelestialBody body, boolean viewable) {

        TableRow row = (TableRow) binding.bodiesTable.getChildAt(body.getIndex()+1);
        CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
        checkBox.setChecked(viewable);

    }

    private class OnCheckListener implements CompoundButton.OnCheckedChangeListener {

        private final int index;

        public OnCheckListener(int index) {
            this.index = index;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            CelestialBody body = CelestialBody.values()[index];

            viewModel.setViewable(body, isChecked);
            onCheckView(body, isChecked, index);

        }

    }

    public void onCheckView(CelestialBody body, boolean isChecked, int index) {

        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
        bundle.putBoolean("CHECKED", isChecked);

        requireActivity().getSupportFragmentManager().setFragmentResult("E_"+body.getName(), bundle);

    }

}