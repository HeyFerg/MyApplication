package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    ArrayList<LatLng> mapMarkers;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Marker currentPosition;
    public static final int REQUEST_LOCATION_CODE = 99;
    private FusedLocationProviderClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        mapMarkers = new ArrayList<LatLng>();
        if (Build.VERSION.SDK_INT > -Build.VERSION_CODES.M) {
            checkLocationPermissions();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

                if (mapMarkers.size() > 1) {
                    mapMarkers.clear();
                    mMap.clear();
                }

                mapMarkers.add(marker);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(marker);

                if (mapMarkers.size() == 1) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else if (mapMarkers.size() == 2) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                }

                mMap.addMarker(markerOptions);

                if(mapMarkers.size() == 2){
                    String url = retrieveURL(mapMarkers.get(0), mapMarkers.get(1));
                    retrieveDirections retrieveDirections = new retrieveDirections();
                    Log.d("gmaps", "Exectuing retrieveDirections with url:" + url);
                    retrieveDirections.execute(url);
                }

            }
        });
    }

    private String retrieveURL (LatLng userOrigin, LatLng userDestination){

        String str_origin = "origin=" + userOrigin.latitude + "," + userOrigin.longitude;

        String str_destination = "destination=" + userDestination.latitude + "," + userDestination.longitude;

        String sensor = "sensor=false";

        String mode = "mode=walking";

        String parameters = str_origin + "&" + str_destination + "&" + sensor + "&" + mode;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output+ "?" + parameters + "&key=AIzaSyCxrlZQCSiN2bNOi7Dv8YsyBCWaR5fh2iA";
        // String url = "https://www.google.com/maps/dir/?api=1&" + parameters;
        // https://maps.googleapis.com/maps/api/directions
        return url;
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

    private String DirectionsRequest(String getURL) throws IOException{

        String response  = "";
        InputStream inputStream = null;
        HttpURLConnection conn = null;

        try{
            URL url = new URL(getURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            inputStream = conn.getInputStream();
            InputStreamReader in  = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(in);

            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            response = sb.toString();
            br.close();
            in.close();
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            if(inputStream != null)
            {
                inputStream.close();
            }
            assert conn != null;
            conn.disconnect();
        }
        return response;
    }

    private class retrieveDirections extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            try{
                response = DirectionsRequest(strings[0]);
                Log.d("gmaps", "Directions is: " + strings[0]);
            } catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            Log.d("gmaps", "returned from retrieveDirections: " + s);
            super.onPostExecute(s);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(s);
        }
    }

    // Below is line 267, where I have created the ParserTask where one of the errors is highlighted in the log.
    @SuppressLint("StaticFieldLeak")
    public class ParserTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings){

            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                Log.d("gmaps", "Strings[0] is: " + strings[0]);
                jsonObject = new JSONObject(strings[0]);
                DataParser dataParser = new DataParser();
                routes = dataParser.parse(jsonObject);
                Log.d("gmaps", "is routes null? " + routes);
            } catch (JSONException e) {
                Log.d("gmaps", "JSONException");
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> lists){

            ArrayList points;

            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path: lists){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                // Line 299, double lon = Double.parseDouble(point.get("lon")); is the line that is also throwing an error. 
                for(HashMap<String, String> point : path){
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            }

            if(polylineOptions != null){
                mMap.addPolyline(polylineOptions);
            } else{
                Toast.makeText(getApplicationContext(), "Directions not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}





