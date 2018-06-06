package com.gionee.autotest.traversal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.util.Constant;

import java.io.File;

import butterknife.Bind;

/**
 * Created by viking on 9/13/17.
 * <p>
 * show local html content
 */

public class WebviewActivity extends BaseActivity {

    String html;

    @Bind(R.id.webView)
    WebView mWebView;

    @Override
    protected int layoutResId() {
        return R.layout.layout_webview;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        if (data == null || data.getStringExtra("html") == null) {
            Log.i(Constant.TAG, "data is null in webview activity");
            finish();
            return;
        }
        html = data.getStringExtra("html");

        File path = new File(html);
        if (!path.exists()) {
            Log.i(Constant.TAG, "html is null in webview activity");
            finish();
            return;
        }

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("file:///" + path.getAbsolutePath());
    }


    private class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
