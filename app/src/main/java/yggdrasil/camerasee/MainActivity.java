package yggdrasil.camerasee;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import yggdrasil.camerasee.ui.DisplayFragment;


public class MainActivity extends Activity {


    public static String selected_size_camera0;
    public static String selected_size_camera1;

    ArrayList<HashMap<String, Size>> arraylist_camera_0;
    ArrayList<HashMap<String, Size>> arraylist_camera_1;

    private static final int INIT_OK = 1;
    private static final int INIT_BAD = 2;
    private Dialog mDialog;
    SharedPreferences sharedPref;


    static Size Lagist_Size_camera0;
    static Size Lagist_Size_camera1;


    private final static int PERMISSIONS_REQUEST_CODE = 100;






    @Override
    protected void onResume() {
        super.onResume();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        selected_size_camera0 = sharedPref.getString("selected_size_camera_0",String.valueOf(Lagist_Size_camera0));
        selected_size_camera1 = sharedPref.getString("selected_size_camera_1",String.valueOf(Lagist_Size_camera1));
        Log.d("메인액티비티","camera0 : " +selected_size_camera0 +""+"camera1 : "+ selected_size_camera1);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);









        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                // 런타임 퍼미션 처리 필요

                int hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                        && hasWriteExternalStoragePermission==PackageManager.PERMISSION_GRANTED){
                    ;//이미 퍼미션을 가지고 있음


                    if (!PreferenceHelper.checkFirstInit(MainActivity.this)) {
                        new Thread(new MyRunnable()).start();
                    }
                                    }
                else {
                    //퍼미션 요청
                    ActivityCompat.requestPermissions( this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_CODE);
                }
            }
            else{

            }


        } else {

        }


    }






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length > 0) {

            int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            int hasWriteExternalStoragePermission =
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                    && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED ){

                //이미 퍼미션을 가지고 있음
                doRestart(this);
            }
            else{
                checkPermissions();
            }
        }

    }


    public static void doRestart(Context c) {
        //http://stackoverflow.com/a/22345538
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted
                        // after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr =
                                (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, " +
                                "mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int hasWriteExternalStoragePermission =
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA);
        boolean writeExternalStorageRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && cameraRationale)
                || (hasWriteExternalStoragePermission== PackageManager.PERMISSION_DENIED
                && writeExternalStorageRationale))
            showDialogForPermission("어플리케이션 실행을 위해 권한을 허용해주세요.");

        else if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && !cameraRationale)
                || (hasWriteExternalStoragePermission== PackageManager.PERMISSION_DENIED
                && !writeExternalStorageRationale))
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");

        else if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                || hasWriteExternalStoragePermission== PackageManager.PERMISSION_GRANTED ) {
            doRestart(this);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //퍼미션 요청
                ActivityCompat.requestPermissions( MainActivity.this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
    private AppCompatActivity mActivity;
    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    private static final String TAG = "MainActivity";

    class MyRunnable implements Runnable {

        @SuppressWarnings("ResourceType")
        @Override
        public void run() {
            try {
                sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String[] cameraIds = manager.getCameraIdList();
                if (cameraIds != null && cameraIds.length > 0) {

                    if (cameraIds[0] != null) {
                        CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[0]);

                        StreamConfigurationMap map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        //TODO 출력 가능한 사진의 해상도만 출력.
                        AvailableSensorSize = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
                        ArrayList<Double> sizeRatio = new ArrayList<>();

                        for (int i = 0; i < AvailableSensorSize.size(); i++) {
                            double ratio = (double) AvailableSensorSize.get(i).getWidth() / AvailableSensorSize.get(i).getHeight();
                            sizeRatio.add(Math.round(ratio * 100) / 100.0);
                            Long resizeH = Math.round(DSI_width * Math.round(ratio * 100) / 100.0);

                        }
                        arraylist_camera_0 = new ArrayList<HashMap<String, Size>>();
                        for (int i = 0; i < 12; i++) {
                            HashMap<String, Size> map_size_camera0 = new HashMap<String, Size>();
                            map_size_camera0.put("Size_camera_0",AvailableSensorSize.get(i));
                            arraylist_camera_0.add(map_size_camera0);
                        }

                        Lagist_Size_camera0 = Collections.max(
                                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                                new CompareSizesByArea());

                        Log.d("로그 Lagist_Size_camera0",String.valueOf(Lagist_Size_camera0));

                        SharedPreferences.Editor editor = sharedPref.edit();
                        Gson gson = new Gson();
                        JsonArray jsonArray = gson.toJsonTree(arraylist_camera_0).getAsJsonArray();
                        String sizecamera0 = gson.toJson(jsonArray);

                        editor.putString("size_camera_0", sizecamera0).apply();




                        Log.d("mainActivity 카메라0 사이즈 ", String.valueOf(AvailableSensorSize));
                        Log.d("mainActivity 카메라0 비율", String.valueOf(sizeRatio));







                    }
                    if (cameraIds[1] != null) {
                        CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[1]);
                        StreamConfigurationMap map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);


                        //TODO 출력 가능한 사진의 해상도만 출력.
                        AvailableSensorSize = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
                        ArrayList<Double> sizeRatio = new ArrayList<>();

                        for (int i = 0; i < AvailableSensorSize.size(); i++) {
                            double ratio = (double) AvailableSensorSize.get(i).getWidth() / AvailableSensorSize.get(i).getHeight();
                            sizeRatio.add(Math.round(ratio * 100) / 100.0);
                            Long resizeH = Math.round(DSI_width * Math.round(ratio * 100) / 100.0);

                        }

                        arraylist_camera_1 = new ArrayList<HashMap<String, Size>>();
                        for (int i = 0; i < 12; i++) {
                            HashMap<String, Size> map_size_camera1 = new HashMap<String, Size>();
                            map_size_camera1.put("Size_camera_1",AvailableSensorSize.get(i));


                            arraylist_camera_1.add(map_size_camera1);
                        }

                        Lagist_Size_camera1 = Collections.max(
                                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                                new CompareSizesByArea());

                        Log.d("로그 Lagist_Size_camera1",String.valueOf(Lagist_Size_camera1));

                        SharedPreferences.Editor editor = sharedPref.edit();
                        Gson gson = new Gson();
                        JsonArray jsonArray = gson.toJsonTree(arraylist_camera_1).getAsJsonArray();
                        String sizecamera1 = gson.toJson(jsonArray);

                        editor.putString("size_camera_1", sizecamera1).apply();
                        Log.d("mainActivity 카메라1 사이즈 ", String.valueOf(AvailableSensorSize));
                        Log.d("mainActivity 카메라1 비율", String.valueOf(sizeRatio));




                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();

                return;
            }

            getFragmentManager().beginTransaction().replace(R.id.frame_main,
                    DisplayFragment.newInstance(String.valueOf(Lagist_Size_camera0), String.valueOf(Lagist_Size_camera1))).commitAllowingStateLoss();
        }
    }


    private class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }






    static int DSI_height, DSI_width;
    static List<Size> AvailableSensorSize;










}
