package com.nagato.agent.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nagato.agent.R;

/**
 * Chat tab: WebView pointing to hermes-agent webui on localhost:8787.
 * Shows a progress/loading state until the agent is ready.
 */
public class ChatFragment extends Fragment {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private static final String AGENT_URL = "http://localhost:8787";

    public ChatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mWebView = view.findViewById(R.id.chat_webview);
        mProgressBar = view.findViewById(R.id.chat_progress);
        mStatusText = view.findViewById(R.id.chat_status_text);

        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoading(true);
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                showLoading(false);
            }

            @Override
            public void onReceivedError(android.webkit.WebView view, int errorCode,
                                       String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showError("Agent not running. Start it in the Terminal tab.");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.loadUrl(AGENT_URL);
        }
    }

    private void showLoading(boolean loading) {
        if (mProgressBar == null || mStatusText == null || mWebView == null) return;
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        mStatusText.setVisibility(loading ? View.VISIBLE : View.GONE);
        mWebView.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (loading) mStatusText.setText("Connecting to Nagato Agent...");
    }

    private void showError(String msg) {
        if (mProgressBar == null || mStatusText == null || mWebView == null) return;
        mProgressBar.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mStatusText.setVisibility(View.VISIBLE);
        mStatusText.setText(msg);
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.loadUrl("about:blank");
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroyView();
    }
}
