package yggdrasil.camerasee.settings;

import android.app.Activity;
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
import yggdrasil.camerasee.adpater.List_size_C1_Adapter;

/**
 * Created by dlrud on 2018-02-06.
 */

public class SizeCamera1 extends Activity {

    static String SIZE_CAMERA_1 = "size_camera_1";
    List_size_C1_Adapter mAdapter;
    ListView listView;

    SharedPreferences sharedPref;
    ArrayList<HashMap<String, Size>> arraylist_sizecamera1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        listView = (ListView) findViewById(R.id.List_size_back_camera);

        Gson gson = new Gson();


        if (sharedPref.contains("size_camera_1") && sharedPref.getString("size_camera_1", "").length() > 0) {
            String sizeCamera1 = sharedPref.getString("size_camera_1", "");
            //Log.d("로그 해쉬 확인 getArrayList sharedPref", PrefDB);
            Type type = new TypeToken<ArrayList<HashMap<String, Size>>>() {
            }.getType();

            arraylist_sizecamera1 = gson.fromJson(sizeCamera1, type);
            mAdapter = new List_size_C1_Adapter(this, arraylist_sizecamera1,sharedPref);

            Log.d("로그 카메라 사이즈",String.valueOf(arraylist_sizecamera1));
            listView.setAdapter(mAdapter);




        }
    }
}
