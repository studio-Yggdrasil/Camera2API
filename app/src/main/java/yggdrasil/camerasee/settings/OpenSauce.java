package yggdrasil.camerasee.settings;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import yggdrasil.camerasee.R;

/**
 * Created by dlrud on 2017-06-13.
 */

public class OpenSauce extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensauce);
        WebView webview = (WebView) findViewById(R.id.opensauce);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVerticalScrollBarEnabled(true);

        String HTML= getString(R.string.OpenSauce);
        webview.loadData(HTML, "text/html; charset=utf-8", "utf-8");
    }
}
