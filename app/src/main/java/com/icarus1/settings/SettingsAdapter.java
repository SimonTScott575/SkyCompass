package com.icarus1.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.icarus1.R;
import com.icarus1.databinding.ViewSettingsEntryBinding;

public class SettingsAdapter extends ListAdapter<Integer, SettingsViewHolder> {

    private LayoutInflater inflater;

    public SettingsAdapter(LayoutInflater inflater) {
        super(DIFF_CALLBACK);
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewSettingsEntryBinding binding = ViewSettingsEntryBinding.inflate(inflater, parent, false);
        return new SettingsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        holder.setPos(position);
    }

    private static final DiffUtil.ItemCallback<Integer> DIFF_CALLBACK = new DiffUtil.ItemCallback<Integer>() {

        @Override
        public boolean areItemsTheSame(@NonNull Integer oldColor,
                                       @NonNull Integer newColor) {
            return oldColor.equals(newColor);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Integer oldColor,
                                          @NonNull Integer newColor) {
            return areItemsTheSame(oldColor, newColor);
        }

    };

}

class SettingsViewHolder extends RecyclerView.ViewHolder {

    private static String[] settings = new String[]{
        "Display",
        "About"
    };
    private static int[] settings2 = new int[]{
        R.id.navigation_settings_action_settings_to_display,
        R.id.navigation_settings_action_settings_to_about
    };
    private static int[] settings3 = new int[]{
        R.drawable.settings_display,
        R.drawable.settings_about
    };

    private ViewSettingsEntryBinding binding;
    private OnClick onClick;
    private int pos;

    SettingsViewHolder(ViewSettingsEntryBinding binding) {
        super(binding.getRoot());

        this.binding = binding;
        onClick = new OnClick();

    }

    public void setPos(int pos) {

        this.pos = pos;
        binding.textView3.setText(settings[pos]);
        binding.imageView.setImageDrawable(binding.imageView.getResources().getDrawable(settings3[pos]));
        binding.getRoot().setOnClickListener(onClick);

    }

    private class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Navigation.findNavController(v).navigate(settings2[pos]);

        }
    }

}