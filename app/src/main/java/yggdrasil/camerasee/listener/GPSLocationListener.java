package yggdrasil.camerasee.listener;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import yggdrasil.camerasee.ui.DisplayFragment;

/**
 * Created by dlrud on 2018-02-06.
 */

public class GPSLocationListener implements LocationListener{


    private LocationManager locationManager;


    private long startTime = -1;
    private static Location beforeLocation;
    private static Location location;
    private Location curLocation;

    public GPSLocationListener(LocationManager locationManager, Location location) {
        this.locationManager = locationManager;
        this.location = location;

    }



    @Override
    public void onLocationChanged(Location location) {
// GPS 변경에 따른 코딩 구현.

        if (startTime == -1) {
            startTime = location.getTime();
        }

        // 현재 위치 거리 및 속도 구하기.
        if (location !=null) {
            beforeLocation = location;
        } else {
        }
// 전 위치 저장.


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @SuppressLint("MissingPermission")

    public Location getLocation() {

        //Criteria 클래스를 이용하여 요구조건을 명시하여, 가장 적합한 기술을 결정
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);


        //true=현재 이용가능한 공급자 제한 (return String)
        //false (return List<String>)
        String Provider = locationManager.getBestProvider(criteria, true);
        if (!locationManager.isProviderEnabled(Provider) && locationManager.getLastKnownLocation(Provider) != null) {
            locationManager.requestLocationUpdates(Provider, 10000, 10, this);

        } else {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            Provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(Provider, 10000, 10, this);

        }

        location = locationManager.getLastKnownLocation(Provider);

        if(location != null) {
            beforeLocation = location;
            DisplayFragment.setLocation(location);
            DisplayFragment.setBeforeLocation(location);
            //Toast.makeText( "위도:" + String.valueOf(location.getLatitude()) + "\n" + "경도:" + String.valueOf(location.getLongitude()), 3000).show();
            return location;
        } else {

            DisplayFragment.setLocation(beforeLocation);
            DisplayFragment.setBeforeLocation(beforeLocation);
            return beforeLocation;
        }



    }
}
