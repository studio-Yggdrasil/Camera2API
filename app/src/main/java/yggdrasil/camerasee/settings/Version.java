package yggdrasil.camerasee.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import yggdrasil.camerasee.R;

/**
 * Created by dlrud on 2017-06-17.
 */

public class Version extends Activity {

    public static String VersionMarket, Version;
    AlertDialog.Builder alt_bld;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        //new Versioncheck().execute();

        //textVersionMarket.setText("version."+ VersionMarket);
        //Log.d("로그","버전마켓 onCreate" +VersionMarket);

    }


    public class Versioncheck extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public String doInBackground(Void... params) {
            // Confirmation of market information in the Google Play Store
            try {
                Document doc = Jsoup
                        .connect(
                                "https://play.google.com/store/apps/details?id=studioyggdrasil.onepnu")
                        .get();
                Elements Version = doc.select(".content");
                for (Element v : Version) {
                    if (v.attr("itemprop").equals("softwareVersion")) {
                        VersionMarket = v.text();

                    }
                }
                //Log.d("로그","버전체크 마켓" +VersionMarket);
                return VersionMarket;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public void onPostExecute(String result) {
           // Version check the execution application.
            PackageInfo pi = null;
            try {
                pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            TextView textVersionMarket = (TextView) findViewById(R.id.versionMarket);
            textVersionMarket.setText("version."+ VersionMarket);

            Version = pi.versionName;
            //Log.d("로그","버전체크 패키지" + Version);
            VersionMarket = result;

            if (Version.equals(VersionMarket)) {
                Button button = (Button) findViewById(R.id.button_update);
                button.setText("현재 최신버전입니다.");
                button.setClickable(false);

            }

                if (!Version.equals(VersionMarket)) {
                    Button button = (Button) findViewById(R.id.button_update);
                    button.setText("업데이트가 필요합니다.");
                    button.setClickable(true);
                    button.setOnClickListener(new View.OnClickListener() {
                               public void onClick(View v) {
                                   Uri uri = Uri.parse("market://details?id=studioyggdrasil.onepnu");
                                   Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                   startActivity(intent);
                                   finish();
                               }
                   });
        }
            super.onPostExecute(result);

        }

    }

}






