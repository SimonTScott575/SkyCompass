package com.icarus1.selectbodies;

import androidx.fragment.app.FragmentManager;
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
import com.icarus1.compass.CelestialObject;
import com.icarus1.databinding.FragmentSelectBodiesBinding;
import com.icarus1.databinding.ViewSelectableBodyBinding;
import com.icarus1.util.Debug;

import java.util.Arrays;
import java.util.List;

public class SelectBodiesFragment extends Fragment {

    private SelectBodiesViewModel viewModel;
    private FragmentSelectBodiesBinding binding;
    private boolean destroyedView;
    private boolean restoredState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoredState = savedInstanceState != null;

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        super.onCreateView(inflater, container, savedInstanceState);

        restoredState |= destroyedView;

        viewModel = new ViewModelProvider(this).get(SelectBodiesViewModel.class);
        binding = FragmentSelectBodiesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<CelestialObject> nonPlanets = Arrays.asList(CelestialObject.nonPlanets());

        for (CelestialObject body : CelestialObject.values()) {

            ViewSelectableBodyBinding selectableBodyBinding = ViewSelectableBodyBinding.inflate(getLayoutInflater(), binding.bodiesTable, true);
            selectableBodyBinding.name.setText(body.getName());
            selectableBodyBinding.checkBox.setOnCheckedChangeListener(new OnCheckListener(body));
            selectableBodyBinding.circle.setColorFilter(body.getPaint().getColor());

            if (!restoredState){
                setViewable(body, nonPlanets.contains(body));
            } else {
                setViewable(body, viewModel.getViewable(body));
            }

        }

    }

    @Override
    public void onDestroyView() {

        destroyedView = true;

        super.onDestroyView();
    }

    private static int getTableIndex(CelestialObject body) {
        return body.ordinal();
    }

    public final boolean getViewable(CelestialObject body) {
        return viewModel.getViewable(body);
    }

    public final void setViewable(CelestialObject body, boolean viewable) {

        TableRow row = (TableRow) binding.bodiesTable.getChildAt(getTableIndex(body)+1);
        CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
        checkBox.setChecked(viewable);

    }

    private class OnCheckListener implements CompoundButton.OnCheckedChangeListener {

        private final CelestialObject body;

        public OnCheckListener(@NonNull CelestialObject body) {
            this.body = body;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            viewModel.setViewable(body, isChecked);
            onCheckView(body, isChecked);

        }

    }

    public void onCheckView(CelestialObject body, boolean isChecked) {

        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", getTableIndex(body));
        bundle.putBoolean("CHECKED", isChecked);

        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("E_" + body.getName(), bundle);
        } catch (NoParentFragmentManagerAttached e) {
            Debug.error(e);
        }

    }

    private FragmentManager getParentFragmentManagerOrThrowException()
    throws NoParentFragmentManagerAttached {

        try {
            return getParentFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoParentFragmentManagerAttached();
        }

    }

    private static class NoParentFragmentManagerAttached extends Debug.Exception {
        public NoParentFragmentManagerAttached() {
            super("No parent fragment manager attached.");
        }
    }

}