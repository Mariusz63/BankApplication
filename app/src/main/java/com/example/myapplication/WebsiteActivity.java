package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class WebsiteActivity extends AppCompatActivity {
    private static final String TAG = "WebsiteActivity";

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);

        webView = (WebView) findViewById(R.id.webView);
    }
}

// 1:18:30