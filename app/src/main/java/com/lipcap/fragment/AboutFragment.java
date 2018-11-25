package com.lipcap.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutFragment extends BaseFragment {


    @BindView(R.id.web_view)
    WebView mWebView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_about, container, false);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, rootView);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(rootView);

        /*For focus current fragment*/
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    default:
                        v.performClick();
                }
                return true;
            }
        });


        return rootView;
    }


    /*Fragment manual onResume*/
    @Override
    public void onFragmentResume() {

        /* If the value of visibleInt is zero,  the view will set gone. Or if the value of visibleInt is one,  the view will set visible. Or else, the view will set gone*/
        if (getActivity() != null) {
            initView();
        }
    }


    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();

        DialogManager.getInstance().showProgress(getActivity());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(true);
        mWebView.loadUrl(AppConstants.ABOUT_WEB);

    }



    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mWebView.setVisibility(View.VISIBLE);
            DialogManager.getInstance().hideProgress();

        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            stopWebView();
        }
    }
    private void stopWebView() {
        DialogManager.getInstance().hideProgress();
        mWebView.stopLoading();
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.loadUrl("");
            mWebView.stopLoading();
        }
    }



}

