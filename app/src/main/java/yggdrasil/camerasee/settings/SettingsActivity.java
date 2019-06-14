package yggdrasil.camerasee.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.ui.DisplayFragment;

/**
 * Created by dlrud on 2018-01-13.
 */

public class SettingsActivity extends PreferenceActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }

    public static class SettingsFragment extends PreferenceFragment {

        SharedPreferences sharedPref;
        String size_camera_0, size_camera_1;
        Preference size0, size1;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());


        }

        @Override
        public void onResume() {
            super.onResume();
            size_camera_0 = sharedPref.getString("selected_size_camera_0", DisplayFragment.getLagistSizeCamera0());
            size0 = findPreference("SizeCamera0");
            size0.setSummary(size_camera_0);

            size_camera_1 = sharedPref.getString("selected_size_camera_1",DisplayFragment.getLagistSizeCamera1());
            size1 = findPreference("SizeCamera1");
            size1.setSummary(size_camera_1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

