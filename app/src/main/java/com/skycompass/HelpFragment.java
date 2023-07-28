package com.skycompass;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

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

        boolean nightMode = ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        if (nightMode) {
            binding.webView.setBackgroundColor(requireContext().getColor(R.color.grey_dark));
        } else {
            binding.webView.setBackgroundColor(requireContext().getColor(R.color.cream_light));
        }

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(requireContext()))
                .build();
        binding.webView.setWebViewClient(new LocalContentWebViewClient(assetLoader) {
            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
                if ("appassets.androidplatform.net".equals(request.getUrl().getHost())) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                startActivity(intent);
                return true;
            }
        });

        binding.webView.loadUrl("https://appassets.androidplatform.net/assets/help/" + "index" + (nightMode ? "-night" : "") + ".html");

    }

    private static class LocalContentWebViewClient extends WebViewClientCompat {

        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(
            WebView view,
            WebResourceRequest request
        ) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation") // to support API < 21
        public WebResourceResponse shouldInterceptRequest(
            WebView view,
            String url
        ) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }

    }

}