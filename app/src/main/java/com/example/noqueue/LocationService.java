package com.example.noqueue;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.noqueue.Firebase.User;

/**
 * Service for getting GPS location
 */
public class LocationService extends Service {

    private LocationManager locationManager = null;

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private class LocationListener implements android.location.LocationListener {
        User user;

        public LocationListener(String provider) {
            user = new User();
        }

        /** Saves the user's location in the database
         * @param location GPS location
         */
        @Override
        public void onLocationChanged(Location location) {
            user.updateGPS(location.getLatitude(),location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

//        Allow usage of the system's GPS service
        if (locationManager == null)
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 0,
                    locationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i("gps", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("gps", "network provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 0,
                    locationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i("gps", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("gps", "gps provider does not exist " + ex.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            for (LocationListener locationListener : locationListeners) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception ex) {
                    Log.i("gps", "fail to remove location listners, ignore", ex);

                }
            }
        }
    }
}
