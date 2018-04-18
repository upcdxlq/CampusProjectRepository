package com.upc.worldwindx.activities;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.upc.R;

/**
 * Created by Lenovo on 2018/4/9.
 */

public class CodeActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        WebView webView = (WebView) findViewById(R.id.code_view);
        // Enable JavaScript (which is off by default)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Bundle bundle = getIntent().getBundleExtra("arguments");
        if (bundle.containsKey("url")) {
                    String url = bundle.getString("url");
                    webView.loadUrl(url);
        }

    }

        @Override
        protected void onStart() {
            super.onStart();
        }

    }
