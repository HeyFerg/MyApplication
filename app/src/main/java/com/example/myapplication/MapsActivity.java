package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;


import java.util.ArrayList;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;




public class MapsActivity extends AppCompatActivity  implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorEventListener {

    ArrayList<LatLng> mapMarkers;
    private GoogleMap mMap;
    private GoogleApiClient client;
    public static final int REQUEST_LOCATION_CODE = 99;
    private FusedLocationProviderClient locationClient;
    android.hardware.SensorManager SensorManager;
    boolean isRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        final Button stopBtn = (Button) findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSteps();

            }
        });
        final Button startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSteps();
            }
        });


        mapMarkers = new ArrayList<LatLng>();
        if (Build.VERSION.SDK_INT > -Build.VERSION_CODES.M) {
            checkLocationPermissions();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        Sensor StepSensor = SensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (StepSensor != null) {
            SensorManager.registerListener(this, StepSensor, android.hardware.SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "No sensor has been found", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission has been denied", Toast.LENGTH_LONG).show();
                }

        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng marker) {

                if (mapMarkers.size() > 3) {
                    mapMarkers.clear();
                    mMap.clear();
                }

                mapMarkers.add(marker);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(marker);

                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLUE);
                polylineOptions.width(8);
                polylineOptions.addAll(mapMarkers);

                if (mapMarkers.size() == 1) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    markerOptions.title("Waypoint 1");
                } else if (mapMarkers.size() == 2) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    markerOptions.title("Waypoint 2");
                    mMap.addPolyline(polylineOptions);
                } else if (mapMarkers.size() == 3) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    markerOptions.title("Waypoint 3");
                    mMap.addPolyline(polylineOptions);
                } else if (mapMarkers.size() == 4) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    markerOptions.title("Waypoint 4");
                    mMap.addPolyline(polylineOptions);
                }

                mMap.addMarker(markerOptions);

            }
        });
    }


    protected synchronized void buildGoogleApiClient() {

        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {


    }


    public boolean checkLocationPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        TextView steps = (TextView) findViewById(R.id.step_taken);

        if (isRunning) {
            steps.setText(String.valueOf(sensorEvent.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
//
    }

    public void stopSteps() {
        SensorManager.unregisterListener(this);
        Toast.makeText(this, "Step counter has been deactivated", Toast.LENGTH_SHORT).show();
    }

    public void startSteps() {
        Sensor StepSensor = SensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        SensorManager.registerListener(this, StepSensor, android.hardware.SensorManager.SENSOR_DELAY_UI);
        Toast.makeText(this, "Step counter has been activated", Toast.LENGTH_SHORT).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logbook) {
            Intent intent = new Intent(this, LogbookActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        return true;
    }
}






