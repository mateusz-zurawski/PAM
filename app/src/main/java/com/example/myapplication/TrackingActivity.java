package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class TrackingActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 5;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;

    TextView tv_lat,tv_lon,tv_altitude,tv_accutancy,tv_speed,tv_sensor,tv_updates,tv_address;
    Switch sw_gps;
    Button btn_startStopActivity;



    //Location request
    Boolean updateOn = false;

    // current location
    Location currentLocation;

    //list of saved location
    List<Location> savedLocation;

    LocationRequest locationRequest;

    //Google APi for localization service
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        //give each UI variable a value
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accutancy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);

        btn_startStopActivity = findViewById(R.id.startStopActivity);

        //set all prop of LocationRequest
        locationRequest =  new LocationRequest();
        // how often does the default location check occure?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL); // Defaul update interwal
        // how often the location check occure when seet to most frequent update
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL); //Fast update interval

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // ecen ther is triggered whenever the update interval is met
        locationCallBack = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //save the location result
                updateUIValue(locationResult.getLastLocation());
            }
        };

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        updateGPS();
    }// end of create method

    private void stopLocationTracking() {
        tv_lat.setText("NOT tracking location");
        tv_lon.setText("NOT tracking location");
        tv_speed.setText("NOT tracking location");
        tv_accutancy.setText("NOT tracking location");
        tv_altitude.setText("NOT tracking location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

//        tv_updates.setText("Location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.myLooper());
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else {
                    Toast.makeText(this,"This app require permission to be granted in order to work proprety",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS(){
        // get permission from the user to track GPS
        // get current location from the fused client
        // update UI

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(TrackingActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            // user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we goot permission. Put the variables of location. XXX to the US components

                    updateUIValue(location);
                    currentLocation = location;
                    Log.v("INFO", " ---> "+String.valueOf(currentLocation.getLatitude()));


                }
            });

        }
        else {
            //permission not granted yet
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);

            }
        }


    }

    private void updateUIValue(Location location) {
        // update all of the text view object with new location
        try{
            tv_lat.setText(String.valueOf(location.getLatitude()));
            Log.v("INFO", String.valueOf(location.getLatitude()));
            tv_lon.setText(String.valueOf(location.getLongitude()));
            tv_accutancy.setText(String.valueOf(location.getAccuracy()));

            if (location.hasAltitude()){
                tv_altitude.setText(String.valueOf(location.getAltitude()));
            }
            else {
                tv_altitude.setText("Not avaiable");
            }
            if (location.hasSpeed()){
                tv_speed.setText(String.valueOf(location.getSpeed()));
            }
            else {
                tv_speed.setText("Not avaiable");
            }


        }catch (NullPointerException e){

        }
//        updateGPS();
    }

    public void onClickStartStopGPS(View view) {
        if (updateOn == false) {
            //turn on location trecking
            updateOn = true;
            btn_startStopActivity.setText("Stop");
            startLocationUpdates();
        }else {
            // trun on location tracking
            updateOn = false;
            btn_startStopActivity.setText("Start");
            stopLocationTracking();
        }
    }
}