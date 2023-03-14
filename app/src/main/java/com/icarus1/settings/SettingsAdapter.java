package com.icarus1.settings;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.icarus1.R;
import com.icarus1.databinding.ViewSettingsEntryBinding;

public class SettingsAdapter extends ListAdapter<Integer, SettingsViewHolder> {

    private final LayoutInflater inflater;

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
        public boolean areItemsTheSame(
            @NonNull Integer oldItem,
            @NonNull Integer newItem
        ) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull Integer oldItem,
            @NonNull Integer newItem
        ) {
            return areItemsTheSame(oldItem, newItem);
        }

    };

}

class SettingsViewHolder extends RecyclerView.ViewHolder {

    private final ViewSettingsEntryBinding binding;
    private final OnClick onClick;
    private int pos;

    SettingsViewHolder(ViewSettingsEntryBinding binding) {
        super(binding.getRoot());

        this.binding = binding;
        onClick = new OnClick();

    }

    public void setPos(int pos) {

        SettingsData data = SettingsData.values()[pos];
        Drawable drawable = ResourcesCompat.getDrawable(
            binding.imageView.getResources(),
            data.getIcon(),
            binding.imageView.getContext().getTheme()
        );

        this.pos = pos;
        binding.textView3.setText(data.getName());
        binding.imageView.setImageDrawable(drawable);
        binding.getRoot().setOnClickListener(onClick);

    }

    private class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            SettingsData data = SettingsData.values()[pos];
            Navigation.findNavController(v).navigate(data.getAction());

        }
    }

}

enum SettingsData {

    DISPLAY(new DisplayFactory()),
    ABOUT(new AboutFactory());

    private String name;
    private int icon;
    private int action;

    SettingsData(Factory factory) {
        factory.init(this);
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public int getAction() {
        return action;
    }

    private interface Factory {
        void init(SettingsData data);
    }

    private static class DisplayFactory implements Factory {
        @Override
        public void init(SettingsData data) {
            data.name = "Display";
            data.icon = R.drawable.settings_display;
            data.action = R.id.navigation_settings_action_settings_to_display;
        }
    }

    private static class AboutFactory implements Factory {
        @Override
        public void init(SettingsData data) {
            data.name = "About";
            data.icon = R.drawable.settings_about;
            data.action = R.id.navigation_settings_action_settings_to_about;
        }
    }

}