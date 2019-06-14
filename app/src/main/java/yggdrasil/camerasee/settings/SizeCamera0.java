package yggdrasil.camerasee.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.adpater.List_size_C0_Adapter;

/**
 * Created by dlrud on 2018-02-06.
 */

public class SizeCamera0 extends Activity {

    static String SIZE_CAMERA_0 = "size_camera_0";
    List_size_C0_Adapter mAdapter;
    ListView listView;
    Context context;
    SharedPreferences sharedPref;
    ArrayList<HashMap<String, Size>> arraylist_sizecamera0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        listView = (ListView) findViewById(R.id.List_size_back_camera);

        Gson gson = new Gson();


        if (sharedPref.contains("size_camera_0") && sharedPref.getString("size_camera_0", "").length() > 0) {
            String sizeCamera0 = sharedPref.getString("size_camera_0", "");
            //Log.d("로그 해쉬 확인 getArrayList sharedPref", PrefDB);
            Type type = new TypeToken<ArrayList<HashMap<String, Size>>>() {
            }.getType();

            arraylist_sizecamera0 = gson.fromJson(sizeCamera0, type);
            mAdapter = new List_size_C0_Adapter(this, arraylist_sizecamera0,sharedPref);

            Log.d("로그 카메라 사이즈",String.valueOf(arraylist_sizecamera0));
            listView.setAdapter(mAdapter);



        }
    }
}
