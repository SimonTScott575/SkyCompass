package com.icarus1;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.icarus1.databinding.FragmentHelpBinding;
import com.icarus1.util.Debug;

public class HelpFragment extends Fragment {

    private static final HelpFragment.MenuListener MENU_LISTENER = new HelpFragment.MenuListener();

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

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(requireContext()))
                .build();
        binding.webView.setWebViewClient(new LocalContentWebViewClient(assetLoader));

        binding.webView.loadUrl("https://appassets.androidplatform.net/assets/help/index.html");

    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().addMenuProvider(MENU_LISTENER);

    }

    @Override
    public void onPause() {

        requireActivity().removeMenuProvider(MENU_LISTENER);

        super.onPause();
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

    private static class MenuListener implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        }
        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            try {
                menu.findItem(R.id.menu_item_help).setVisible(false);
            } catch (NullPointerException e) {
                Debug.error(e.getMessage());
            }
        }
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            return false;
        }
    }

}