package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skycompass.databinding.FragmentHelpBinding;

public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHelpBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int i = 0; i < binding.textViewsContainer.getChildCount(); i++) {

            View child = binding.textViewsContainer.getChildAt(i);

            if (child instanceof TextView) {

                TextView textView = (TextView) child;

                int color = textView.getCurrentTextColor();
                int margin = textView.getLineHeight() / 4;
                int height = textView.getLineHeight() / 8;
                int gap = textView.getLineHeight() / 4;

                String text = textView.getText().toString();

                int newLines = text.split("\n").length - 1;

                SpannableStringBuilder builder = new SpannableStringBuilder();

                for (String line : text.split("\n")) {

                    boolean hasBulletPoint = line.contains("\u2022");
                    line = line.replace("\u2022", "");

                    SpannableString newLine = new SpannableString(line);

                    if (hasBulletPoint) {
                        newLine.setSpan(new LeadingMarginSpan.Standard(margin), 0, newLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        newLine.setSpan(new BulletSpan(gap, color, height), 0, newLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    builder.append(newLine);

                    if (newLines-- > 0)
                        builder.append("\n");

                }

                textView.setText(builder);

            }

        }

        String moreInfoText = binding.moreInfo.getText().toString();

        SpannableString moreInfoSpannable = new SpannableString(moreInfoText);

        moreInfoSpannable.setSpan(new URLSpan("https://github.com/SimonTScott575/SkyCompass"), 0, moreInfoText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.moreInfo.setText(moreInfoSpannable);
        binding.moreInfo.setMovementMethod(LinkMovementMethod.getInstance());

    }
}