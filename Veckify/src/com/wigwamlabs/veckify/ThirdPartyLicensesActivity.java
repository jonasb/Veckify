package com.wigwamlabs.veckify;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class ThirdPartyLicensesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_thirdparty_licenses);

        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/licenses.html");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            startActivity(getParentActivityIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
